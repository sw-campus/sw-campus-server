package com.swcampus.domain.storage;

/**
 * 파일 저장소 인터페이스
 */
public interface FileStorageService {

    /**
     * 파일을 저장하고 접근 URL을 반환
     *
     * @param content     파일 바이트 배열
     * @param directory   저장할 디렉토리 경로 (예: "lectures", "teachers")
     * @param fileName    원본 파일명
     * @param contentType MIME 타입
     * @return 저장된 파일의 접근 URL
     */
    String upload(byte[] content, String directory, String fileName, String contentType);

    /**
     * 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 URL
     */
    void delete(String fileUrl);

    /**
     * 민감한 파일을 private bucket에 저장하고 접근 URL을 반환
     *
     * @param content     파일 바이트 배열
     * @param directory   저장할 디렉토리 경로
     * @param fileName    원본 파일명
     * @param contentType MIME 타입
     * @return 저장된 파일의 접근 URL
     */
    String uploadPrivate(byte[] content, String directory, String fileName, String contentType);

    /**
     * private bucket에서 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 URL
     */
    void deletePrivate(String fileUrl);
}
