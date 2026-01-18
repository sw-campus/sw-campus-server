package com.swcampus.infra.s3;

import com.swcampus.domain.member.Role;
import com.swcampus.domain.storage.PresignedUrlService;
import com.swcampus.domain.storage.exception.InvalidStorageCategoryException;
import com.swcampus.domain.storage.exception.StorageAccessDeniedException;
import com.swcampus.domain.storage.exception.StorageBatchLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService implements PresignedUrlService {

    private static final int DEFAULT_EXPIRATION_MINUTES = 15;
    private static final int MAX_BATCH_SIZE = 50;
    private static final Set<String> PRIVATE_PREFIXES = Set.of("certificates/", "employment-certificates/", "members/");
    private static final Set<String> PRIVATE_CATEGORIES = Set.of("certificates", "employment-certificates", "members");
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "lectures", "organizations", "teachers", "banners", "thumbnails", "certificates", "employment-certificates", "members", "posts"
    );
    // 관리자만 업로드 가능한 카테고리
    private static final Set<String> ADMIN_ONLY_CATEGORIES = Set.of("banners");
    // 기관 또는 관리자만 업로드 가능한 카테고리
    private static final Set<String> ORGANIZATION_CATEGORIES = Set.of("lectures", "organizations", "teachers", "thumbnails");

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String publicBucket;

    @Value("${aws.s3.private-bucket}")
    private String privateBucket;

    @Override
    public PresignedUrl getPresignedUrl(String key, boolean isAdmin) {
        if (isPrivateKey(key) && !isAdmin) {
            throw new StorageAccessDeniedException();
        }

        String bucket = isPrivateKey(key) ? privateBucket : publicBucket;
        String url = generatePresignedGetUrl(bucket, key, DEFAULT_EXPIRATION_MINUTES);

        return new PresignedUrl(url, DEFAULT_EXPIRATION_MINUTES * 60);
    }

    @Override
    public Map<String, String> getPresignedUrls(List<String> keys, boolean isAdmin) {
        if (keys.size() > MAX_BATCH_SIZE) {
            throw new StorageBatchLimitExceededException(MAX_BATCH_SIZE);
        }

        Map<String, String> result = new HashMap<>();

        for (String key : keys) {
            if (isPrivateKey(key) && !isAdmin) {
                result.put(key, null);
            } else {
                String bucket = isPrivateKey(key) ? privateBucket : publicBucket;
                String url = generatePresignedGetUrl(bucket, key, DEFAULT_EXPIRATION_MINUTES);
                result.put(key, url);
            }
        }

        return result;
    }

    @Override
    public PresignedUploadUrl getPresignedUploadUrl(String category, String fileName, String contentType, Role role) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new InvalidStorageCategoryException(category);
        }

        validateUploadPermission(category, role);

        String key = generateKey(category, fileName);
        String bucket = PRIVATE_CATEGORIES.contains(category) ? privateBucket : publicBucket;
        String url = generatePresignedPutUrl(bucket, key, contentType, DEFAULT_EXPIRATION_MINUTES);

        return new PresignedUploadUrl(url, key, DEFAULT_EXPIRATION_MINUTES * 60);
    }

    private void validateUploadPermission(String category, Role role) {
        // 관리자는 모든 카테고리 업로드 가능
        if (role == Role.ADMIN) {
            return;
        }

        // 관리자 전용 카테고리 체크
        if (ADMIN_ONLY_CATEGORIES.contains(category)) {
            throw new StorageAccessDeniedException();
        }

        // 기관 전용 카테고리 체크
        if (ORGANIZATION_CATEGORIES.contains(category) && role != Role.ORGANIZATION) {
            throw new StorageAccessDeniedException();
        }

        // 나머지 카테고리(certificates, employment-certificates, members)는 모든 인증 사용자 허용
    }

    private boolean isPrivateKey(String key) {
        return PRIVATE_PREFIXES.stream().anyMatch(key::startsWith);
    }

    private String generatePresignedGetUrl(String bucket, String key, int expirationMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String generatePresignedPutUrl(String bucket, String key, String contentType, int expirationMinutes) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    private String generateKey(String category, String fileName) {
        String extension = getExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return String.format("%s/%s/%s%s", category, date, uuid, extension);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }
}
