package com.swcampus.domain.storage;

import java.util.List;
import java.util.Map;

/**
 * Presigned URL 발급 서비스 인터페이스
 */
public interface PresignedUrlService {

    /**
     * 단일 S3 key에 대한 Presigned GET URL을 발급합니다.
     *
     * @param key     S3 객체 key
     * @param isAdmin 관리자 여부 (Private 파일 접근 권한 체크용)
     * @return Presigned URL 정보
     * @throws com.swcampus.domain.storage.exception.StorageAccessDeniedException Private 파일에 대해 관리자가 아닌 경우
     */
    PresignedUrl getPresignedUrl(String key, boolean isAdmin);

    /**
     * 여러 S3 key에 대한 Presigned GET URL을 일괄 발급합니다.
     * Private 파일에 대해 권한이 없으면 해당 key의 값은 null로 반환됩니다.
     *
     * @param keys    S3 객체 key 목록
     * @param isAdmin 관리자 여부
     * @return key와 Presigned URL의 Map (권한 없는 Private key는 null)
     * @throws com.swcampus.domain.storage.exception.StorageBatchLimitExceededException 요청 개수가 제한을 초과한 경우
     */
    Map<String, String> getPresignedUrls(List<String> keys, boolean isAdmin);

    /**
     * S3 업로드를 위한 Presigned PUT URL을 발급합니다.
     *
     * @param category    파일 카테고리 (lectures, organizations, certificates 등)
     * @param fileName    원본 파일명
     * @param contentType MIME 타입
     * @return Presigned Upload URL 정보
     * @throws com.swcampus.domain.storage.exception.InvalidStorageCategoryException 지원하지 않는 카테고리인 경우
     */
    PresignedUploadUrl getPresignedUploadUrl(String category, String fileName, String contentType);

    /**
     * Presigned GET URL 응답 정보
     *
     * @param url       Presigned URL
     * @param expiresIn 만료 시간 (초)
     */
    record PresignedUrl(String url, int expiresIn) {
    }

    /**
     * Presigned PUT URL 응답 정보
     *
     * @param uploadUrl Presigned PUT URL
     * @param key       생성된 S3 key
     * @param expiresIn 만료 시간 (초)
     */
    record PresignedUploadUrl(String uploadUrl, String key, int expiresIn) {
    }
}
