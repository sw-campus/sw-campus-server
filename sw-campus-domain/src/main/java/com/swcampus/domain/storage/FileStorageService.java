package com.swcampus.domain.storage;

/**
 * 파일 저장소 인터페이스
 */
public interface FileStorageService {
    
    /**
     * 파일을 저장하고 접근 URL을 반환
     *
     * @param content 파일 바이트 배열
     * @param fileName 원본 파일명
     * @param contentType MIME 타입
     * @return 저장된 파일의 접근 URL
     */
    String upload(byte[] content, String fileName, String contentType);
    
    /**
     * 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 URL
     */
    void delete(String fileUrl);
}
