package com.swcampus.infra.s3;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.swcampus.domain.storage.exception.InvalidImageTypeException;
import com.swcampus.domain.storage.presigned.PresignedModels.CompletedPart;
import com.swcampus.domain.storage.presigned.PresignedModels.MultipartInitResponse;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedPart;
import com.swcampus.domain.storage.presigned.PresignedModels.PresignedSingleUpload;
import com.swcampus.domain.storage.presigned.PresignedStoragePort;
import com.swcampus.domain.storage.presigned.StorageAccess;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;

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
        PresignedPutObjectRequest presigned = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(presignedExpiryMinutes))
                .putObjectRequest(putReq)
        );

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

        CreateMultipartUploadResponse init = s3Client.createMultipartUpload(cu.build());
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
            PresignedUploadPartRequest preq = presigner.presignUploadPart(b -> b
                    .signatureDuration(Duration.ofMinutes(presignedExpiryMinutes))
                    .uploadPartRequest(upr)
            );
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

        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(cmu)
                .build());
    }

    @Override
    public void abortMultipart(String key, String uploadId, StorageAccess access) {
        String bucket = chooseBucket(access);
        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build());
    }

    @Override
    public void deleteByUrl(String fileUrl, StorageAccess access) {
        String key = extractKeyFromUrl(fileUrl);
        String bucket = chooseBucket(access);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
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

}
