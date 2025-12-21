package com.swcampus.infra.s3;

import com.swcampus.domain.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.private-bucket}")
    private String privateBucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public String upload(byte[] content, String directory, String fileName, String contentType) {
        return performUpload(content, directory, fileName, contentType, bucketName);
    }

    @Override
    public void delete(String fileUrl) {
        performDelete(fileUrl, bucketName);
    }

    @Override
    public String uploadPrivate(byte[] content, String directory, String fileName, String contentType) {
        return performUpload(content, directory, fileName, contentType, privateBucketName);
    }

    @Override
    public void deletePrivate(String fileUrl) {
        performDelete(fileUrl, privateBucketName);
    }

    private String performUpload(byte[] content, String directory, String fileName, String contentType, String targetBucket) {
        String key = generateKey(directory, fileName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", targetBucket, region, key);
    }

    private void performDelete(String fileUrl, String targetBucket) {
        String key = extractKeyFromUrl(fileUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    private String generateKey(String directory, String fileName) {
        String extension = getExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return String.format("%s/%s/%s%s", directory, date, uuid, extension);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

    private String extractKeyFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                return path.substring(1);
            }
            throw new IllegalArgumentException("Invalid S3 URL: path is empty or root.");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid S3 URL format: " + url, e);
        }
    }
}
