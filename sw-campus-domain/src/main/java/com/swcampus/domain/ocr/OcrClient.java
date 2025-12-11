package com.swcampus.domain.ocr;

import java.util.List;

/**
 * OCR 서버 클라이언트 인터페이스
 */
public interface OcrClient {
    
    /**
     * 이미지에서 텍스트를 추출
     *
     * @param imageBytes 이미지 바이트 배열
     * @param fileName 파일명
     * @return 추출된 텍스트 라인 목록
     */
    List<String> extractText(byte[] imageBytes, String fileName);
}
