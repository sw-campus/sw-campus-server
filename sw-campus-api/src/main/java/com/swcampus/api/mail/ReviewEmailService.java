package com.swcampus.api.mail;

import com.swcampus.domain.auth.MailSender;
import com.swcampus.domain.review.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewEmailService implements EmailService {

    private final MailSender mailSender;

    @Async
    @Override
    public void sendCertificateRejectionEmail(String email) {
        try {
            String subject = "[SW Campus] 수료증 인증이 반려되었습니다";
            String content = """
                <html>
                <body>
                    <h2>수료증 인증 반려 안내</h2>
                    <p>안녕하세요, SW Campus입니다.</p>
                    <p>제출하신 수료증이 검증에 실패했습니다.</p>
                    <p>올바른 수료증을 다시 제출해주세요.</p>
                    <br>
                    <p>감사합니다.</p>
                </body>
                </html>
                """;
            mailSender.send(email, subject, content);
            log.info("수료증 반려 이메일 발송 완료: {}", email);
        } catch (Exception e) {
            log.error("수료증 반려 이메일 발송 실패: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void sendReviewRejectionEmail(String email) {
        try {
            String subject = "[SW Campus] 후기가 반려되었습니다";
            String content = """
                <html>
                <body>
                    <h2>후기 반려 안내</h2>
                    <p>안녕하세요, SW Campus입니다.</p>
                    <p>작성하신 후기가 관리자 검토 결과 반려되었습니다.</p>
                    <p>부적절한 내용이 포함되어 있습니다.</p>
                    <br>
                    <p>감사합니다.</p>
                </body>
                </html>
                """;
            mailSender.send(email, subject, content);
            log.info("후기 반려 이메일 발송 완료: {}", email);
        } catch (Exception e) {
            log.error("후기 반려 이메일 발송 실패: {}", e.getMessage());
        }
    }
}
