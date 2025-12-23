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
        // 이미 가입된 회원이면 인증 불가 (재가입 방지)
        if (memberRepository.existsByEmail(email)) {
            return false;
        }
        return emailVerificationRepository.findByEmailAndVerified(email, true).isPresent();
    }

    public void sendApprovalEmail(String memberEmail, String organizationName) {
        String subject = "[SW Campus] 기관 회원가입 승인 완료";
        String content = buildApprovalEmailContent(organizationName);
        mailSender.send(memberEmail, subject, content);
    }

    public void sendRejectionEmail(String memberEmail, String adminEmail, String adminPhone) {
        String subject = "[SW Campus] 기관 회원가입 반려 안내";
        String content = buildRejectionEmailContent(adminEmail, adminPhone);
        mailSender.send(memberEmail, subject, content);
    }

    private String buildApprovalEmailContent(String organizationName) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #28a745;">SW Campus 기관 회원가입 승인 완료</h2>
                    <p>안녕하세요.</p>
                    <p>신청하신 <strong>%s</strong> 기관 회원가입이 승인되었습니다.</p>
                    <p>이제 SW Campus의 모든 기관 서비스를 이용하실 수 있습니다.</p>
                    <div style="background: #d4edda; padding: 15px; border-radius: 5px; margin: 20px 0; border: 1px solid #c3e6cb;">
                        <p style="margin: 5px 0; color: #155724;"><strong>✓ 강의 등록</strong></p>
                        <p style="margin: 5px 0; color: #155724;"><strong>✓ 기관 정보 수정</strong></p>
                        <p style="margin: 5px 0; color: #155724;"><strong>✓ 기타 기관 전용 서비스</strong></p>
                    </div>
                    <a href="https://swcampus.com" style="display:inline-block;padding:10px 20px;background:#28a745;color:#fff;text-decoration:none;border-radius:5px;">
                        SW Campus 바로가기
                    </a>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">본 메일은 SW Campus에서 발송되었습니다.</p>
                </body>
                </html>
                """.formatted(organizationName);
    }

    private String buildRejectionEmailContent(String adminEmail, String adminPhone) {
        String phoneDisplay = adminPhone != null ? adminPhone : "미등록";
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #333;">SW Campus 기관 회원가입 반려 안내</h2>
                    <p>안녕하세요.</p>
                    <p>신청하신 기관 회원가입이 반려되었습니다.</p>
                    <p>재직증명서 또는 기타 서류에 문제가 있을 수 있습니다.</p>
                    <p>자세한 사항은 아래 관리자에게 문의해 주세요.</p>
                    <div style="background: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>관리자 이메일:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>관리자 연락처:</strong> %s</p>
                    </div>
                    <p>다시 가입을 원하시면 이메일 인증부터 새로 진행해 주세요.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">본 메일은 SW Campus에서 발송되었습니다.</p>
                </body>
                </html>
                """.formatted(adminEmail, phoneDisplay);
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
