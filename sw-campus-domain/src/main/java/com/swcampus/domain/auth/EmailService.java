package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailVerificationExpiredException;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final MailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendVerificationEmail(String email) {
        sendVerificationEmail(email, "personal");
    }

    public void sendVerificationEmail(String email, String signupType) {
        // 이미 가입된 이메일 확인
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        // 기존 인증 정보 삭제 (재발송 대응)
        emailVerificationRepository.deleteByEmail(email);

        // 새 인증 생성
        EmailVerification verification = EmailVerification.create(email);
        emailVerificationRepository.save(verification);

        // 이메일 발송 (signupType을 쿼리 파라미터로 포함)
        String verifyUrl = frontendUrl + "/auth/verify?token=" + verification.getToken() + "&type=" + signupType;
        String subject = "[SW Campus] 이메일 인증";
        String content = buildEmailContent(verifyUrl);

        mailSender.send(email, subject, content);
    }

    public String verifyEmail(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(InvalidTokenException::new);

        // 만료 확인
        if (verification.isExpired()) {
            throw new EmailVerificationExpiredException(verification.getEmail());
        }

        verification.verify();
        emailVerificationRepository.save(verification);
        
        return verification.getEmail();
    }

    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.findByEmailAndVerified(email, true).isPresent();
    }

    private String buildEmailContent(String verifyUrl) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #333;">SW Campus 이메일 인증</h2>
                    <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>
                    <a href="%s" style="display:inline-block;padding:10px 20px;background:#007bff;color:#fff;text-decoration:none;border-radius:5px;">
                        이메일 인증하기
                    </a>
                    <p style="color: #666; margin-top: 20px;">이 링크는 24시간 동안 유효합니다.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">본 메일은 SW Campus 회원가입을 위해 발송되었습니다.</p>
                </body>
                </html>
                """.formatted(verifyUrl);
    }
}
