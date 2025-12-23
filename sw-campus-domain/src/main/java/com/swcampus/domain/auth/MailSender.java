package com.swcampus.domain.auth;

/**
 * 이메일 발송 인터페이스
 * <p>
 * Infrastructure 레이어에서 구현체를 제공합니다.
 */
public interface MailSender {

    /**
     * 이메일을 발송합니다.
     *
     * @param to      수신자 이메일
     * @param subject 제목
     * @param content 본문 (HTML 지원)
     */
    void send(String to, String subject, String content);
}
