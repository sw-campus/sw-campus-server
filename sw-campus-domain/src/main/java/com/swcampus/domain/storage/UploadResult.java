package com.swcampus.domain.storage;

/**
 * 파일 업로드 결과를 담는 객체.
 * URL에서 key를 추출하는 fragile한 로직 대신, 업로드 시점에 두 값을 함께 반환합니다.
 *
 * @param url 저장된 파일의 접근 URL (예: https://bucket.s3.region.amazonaws.com/posts/2024/01/01/uuid.jpg)
 * @param key 저장된 파일의 S3 key (예: posts/2024/01/01/uuid.jpg)
 */
public record UploadResult(String url, String key) {
}
