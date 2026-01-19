package com.swcampus.domain.storage;

import java.io.InputStream;

/**
 * 파일 저장소 인터페이스
 */
public interface FileStorageService {

    /**
     * 파일을 저장하고 업로드 결과(URL과 key)를 반환
     *
     * @param content     파일 바이트 배열
     * @param directory   저장할 디렉토리 경로 (예: "lectures", "teachers")
     * @param fileName    원본 파일명
     * @param contentType MIME 타입
     * @return 저장된 파일의 URL과 key를 포함한 결과
     */
    UploadResult upload(byte[] content, String directory, String fileName, String contentType);

    /**
     * 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 URL
     */
    void delete(String fileUrl);

    /**
     * 민감한 파일을 private bucket에 저장하고 S3 key를 반환
     * <p>
     * <b>주의:</b> 반환되는 key는 직접적인 공개 접근이 불가능하며, 파일을 식별하기 위한 용도로 사용됩니다.
     * 파일에 접근하려면 별도의 인증된 절차(예: pre-signed URL 생성)가 필요합니다.
     *
     * @param content     파일 바이트 배열
     * @param directory   저장할 디렉토리 경로
     * @param fileName    원본 파일명
     * @param contentType MIME 타입
     * @return 저장된 파일의 S3 key (예: "certificates/2024/01/01/uuid.jpg")
     */
    String uploadPrivate(byte[] content, String directory, String fileName, String contentType);

    /**
     * InputStream을 사용하여 민감한 파일을 private bucket에 저장
     * <p>
     * 메모리 효율적인 대용량 파일 업로드를 지원합니다.
     *
     * @param inputStream   파일 입력 스트림
     * @param contentLength 파일 크기 (바이트)
     * @param directory     저장할 디렉토리 경로
     * @param fileName      원본 파일명
     * @param contentType   MIME 타입
     * @return 저장된 파일의 S3 key
     */
    String uploadPrivate(InputStream inputStream, long contentLength, String directory, String fileName, String contentType);

    /**
     * private bucket에서 파일 삭제
     *
     * @param fileKey 삭제할 파일의 S3 key
     */
    void deletePrivate(String fileKey);
}
