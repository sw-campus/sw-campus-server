package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.InvalidPasswordException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final MailSender mailSender;

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    /**
     * 비밀번호 변경 (로그인 상태)
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // OAuth 사용자 체크
        if (member.getPassword() == null) {
            throw new InvalidPasswordException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 정책 검증
        passwordValidator.validate(newPassword);

        // 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.changePassword(encodedPassword);
        memberRepository.save(member);
    }

    /**
     * 임시 비밀번호 발급 (비밀번호 찾기)
     */
    public void issueTemporaryPassword(String email) {
        Member member = memberRepository.findByEmail(email).orElse(null);

        // 사용자 없음 또는 OAuth 사용자 → 조용히 종료 (보안)
        if (member == null || member.getPassword() == null) {
            return;
        }

        // 임시 비밀번호 생성
        String temporaryPassword = generateTemporaryPassword();

        // 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        member.changePassword(encodedPassword);
        memberRepository.save(member);

        // 이메일 발송
        String subject = "[SW Campus] 임시 비밀번호 안내";
        String content = buildTemporaryPasswordEmailContent(temporaryPassword);
        mailSender.send(email, subject, content);
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(TEMP_PASSWORD_CHARS.length());
            sb.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private String buildTemporaryPasswordEmailContent(String temporaryPassword) {
        return """
                <html>
                <body>
                    <h2>SW Campus 임시 비밀번호 안내</h2>
                    <p>안녕하세요, SW Campus입니다.</p>
                    <p>요청하신 임시 비밀번호를 안내해드립니다.</p>
                    <div style="background:#f5f5f5;padding:20px;margin:20px 0;font-size:18px;font-weight:bold;">
                        임시 비밀번호: %s
                    </div>
                    <p style="color:#dc3545;">보안을 위해 로그인 후 반드시 비밀번호를 변경해주세요.</p>
                    <p>본인이 요청하지 않은 경우 이 메일을 무시해주세요.</p>
                    <br/>
                    <p>감사합니다.</p>
                    <p>SW Campus 팀</p>
                </body>
                </html>
                """.formatted(temporaryPassword);
    }
}
