package com.swcampus.domain.review;

/**
 * 이메일 발송 서비스 인터페이스
 */
public interface EmailService {
    void sendCertificateRejectionEmail(String email);
    void sendReviewRejectionEmail(String email);
}
