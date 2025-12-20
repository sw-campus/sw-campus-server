package com.swcampus.infra.s3;

import com.swcampus.domain.storage.presigned.PresignedModels.CompletedPart;
import com.swcampus.domain.storage.presigned.PresignedModels.MultipartInitResponse;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedPart;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedSingleUpload;
import com.swcampus.domain.storage.presigned.PresignedStoragePort;
import com.swcampus.domain.storage.presigned.StorageAccess;
import com.swcampus.domain.storage.exception.InvalidImageTypeException;
import com.swcampus.domain.storage.exception.StorageServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3PresignedStorageService implements PresignedStoragePort {

    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.public-bucket:${aws.s3.bucket}}")
    private String publicBucket;

    @Value("${aws.s3.private-bucket:${aws.s3.bucket}-private}")
    private String privateBucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${storage.upload.presigned.expiryMinutes:10}")
    private int presignedExpiryMinutes;

    @Value("${storage.upload.multipartThresholdMb:8}")
    private int multipartThresholdMb;

    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Override
    public PresignedSingleUpload createSingleUpload(String directory, String fileName, String contentType, Long contentLength, StorageAccess access) {
        validateImage(contentType);
        String key = generateKey(directory, fileName);
        String bucket = chooseBucket(access);

        PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType);
        if (access == StorageAccess.PRIVATE) {
            put = put.serverSideEncryption(ServerSideEncryption.AES256);
        }
        if (contentLength != null) {
            put = put.contentLength(contentLength);
        }

        PutObjectRequest putReq = put.build();
        PresignedPutObjectRequest presigned;
        try {
            presigned = presigner.presignPutObject(b -> b
                    .signatureDuration(Duration.ofMinutes(presignedExpiryMinutes))
                    .putObjectRequest(putReq)
            );
        } catch (S3Exception e) {
            throw new StorageServiceException("S3 presign put failed: " + e.awsErrorDetails().errorMessage(), e.statusCode());
        } catch (SdkClientException e) {
            throw new StorageServiceException("S3 presign client error: " + e.getMessage(), 502);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);

        return new PresignedSingleUpload(
                presigned.url().toString(),
                "PUT",
                headers,
                key,
                buildPublicUrl(bucket, key)
        );
    }

    @Override
    public MultipartInitResponse initiateMultipart(String directory, String fileName, String contentType, long totalSize, StorageAccess access) {
        validateImage(contentType);
        String key = generateKey(directory, fileName);
        String bucket = chooseBucket(access);

        CreateMultipartUploadRequest.Builder cu = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType);
        if (access == StorageAccess.PRIVATE) {
            cu = cu.serverSideEncryption(ServerSideEncryption.AES256);
        }

        CreateMultipartUploadResponse init;
        try {
            init = s3Client.createMultipartUpload(cu.build());
        } catch (S3Exception e) {
            throw new StorageServiceException("S3 create multipart failed: " + e.awsErrorDetails().errorMessage(), e.statusCode());
        } catch (SdkClientException e) {
            int status = inferGatewayStatus(e);
            throw new StorageServiceException("S3 client error during multipart init: " + e.getMessage(), status);
        }
        long partSize = Math.max(5L * 1024 * 1024, multipartThresholdMb * 1024L * 1024L); // at least 5MB per S3 rule
        return new MultipartInitResponse(init.uploadId(), key, partSize, buildPublicUrl(bucket, key));
    }

    @Override
    public List<PresignedPart> signParts(String key, String uploadId, List<Integer> partNumbers, StorageAccess access) {
        String bucket = chooseBucket(access);
        List<PresignedPart> result = new ArrayList<>();
        for (Integer pn : partNumbers) {
            UploadPartRequest upr = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(pn)
                    .build();
            PresignedUploadPartRequest preq;
            try {
                preq = presigner.presignUploadPart(b -> b
                        .signatureDuration(Duration.ofMinutes(presignedExpiryMinutes))
                        .uploadPartRequest(upr)
                );
            } catch (S3Exception e) {
                throw new StorageServiceException("S3 presign upload-part failed: " + e.awsErrorDetails().errorMessage(), e.statusCode());
            } catch (SdkClientException e) {
                int status = inferGatewayStatus(e);
                throw new StorageServiceException("S3 presign client error during upload-part: " + e.getMessage(), status);
            }
            result.add(new PresignedPart(pn, preq.url().toString(), Collections.emptyMap()));
        }
        return result;
    }

    @Override
    public void completeMultipart(String key, String uploadId, List<CompletedPart> parts, StorageAccess access) {
        String bucket = chooseBucket(access);
        CompletedMultipartUpload cmu = CompletedMultipartUpload.builder()
                .parts(parts.stream()
                        .sorted(Comparator.comparingInt(CompletedPart::partNumber))
                        .map(p -> software.amazon.awssdk.services.s3.model.CompletedPart.builder()
                                .eTag(p.eTag())
                                .partNumber(p.partNumber())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        try {
            s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(cmu)
                    .build());
        } catch (S3Exception e) {
            throw new StorageServiceException("S3 complete multipart failed: " + e.awsErrorDetails().errorMessage(), e.statusCode());
        } catch (SdkClientException e) {
            int status = inferGatewayStatus(e);
            throw new StorageServiceException("S3 client error during multipart complete: " + e.getMessage(), status);
        }
    }

    @Override
    public void abortMultipart(String key, String uploadId, StorageAccess access) {
        String bucket = chooseBucket(access);
        try {
            s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .build());
        } catch (S3Exception e) {
            throw new StorageServiceException("S3 abort multipart failed: " + e.awsErrorDetails().errorMessage(), e.statusCode());
        } catch (SdkClientException e) {
            int status = inferGatewayStatus(e);
            throw new StorageServiceException("S3 client error during multipart abort: " + e.getMessage(), status);
        }
    }

    @Override
    public void deleteByUrl(String fileUrl, StorageAccess access) {
        String key = extractKeyFromUrl(fileUrl);
        String bucket = chooseBucket(access);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new StorageServiceException("S3 delete object failed: " + e.awsErrorDetails().errorMessage(), e.statusCode());
        } catch (SdkClientException e) {
            int status = inferGatewayStatus(e);
            throw new StorageServiceException("S3 client error during delete: " + e.getMessage(), status);
        }
    }

    private void validateImage(String contentType) {
        if (!ALLOWED_IMAGE_MIME.contains(contentType)) {
            throw new InvalidImageTypeException("Unsupported image content-type: " + contentType);
        }
    }

    private String chooseBucket(StorageAccess access) {
        return access == StorageAccess.PUBLIC ? publicBucket : privateBucket;
    }

    private String buildPublicUrl(String bucket, String key) {
        // For private bucket this is not publicly accessible but still a canonical URL
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    private String generateKey(String directory, String fileName) {
        String extension = getExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("%s/%s/%s%s", directory, date, uuid, extension);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String ext = dotIndex > 0 ? fileName.substring(dotIndex).toLowerCase(Locale.ROOT) : "";
        // normalize jpeg
        if (ext.equals(".jpeg")) return ".jpg";
        return ext;
    }

    private String extractKeyFromUrl(String url) {
        return url.substring(url.indexOf(".com/") + 5);
    }

    private int inferGatewayStatus(Throwable e) {
        String msg = e.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("timed out") || lower.contains("timeout") || lower.contains("read timed out") || lower.contains("connect timed out")) {
                return 504; // Gateway Timeout
            }
            if (lower.contains("connection refused") || lower.contains("connection reset") || lower.contains("unresolved")) {
                return 502; // Bad Gateway (generic upstream error)
            }
            if (lower.contains("service unavailable") || lower.contains("throttling") || lower.contains("slowdown")) {
                return 503; // Service Unavailable (S3 throttling/slowdown)
            }
        }
        return 502;
    }
}
