package com.swcampus.api.mail;

import com.swcampus.domain.auth.MailSender;
import com.swcampus.domain.auth.exception.MailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@swcampus.com}")
    private String from;

    @Override
    public void send(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);  // HTML

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("이메일 발송에 실패했습니다", e);
        }
    }
}
