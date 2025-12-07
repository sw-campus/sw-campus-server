package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailNotVerifiedException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public Member signup(SignupCommand command) {
        // 1. 중복 이메일 검증
        if (memberRepository.existsByEmail(command.getEmail())) {
            throw new DuplicateEmailException(command.getEmail());
        }

        // 2. 이메일 인증 여부 확인
        emailVerificationRepository.findByEmailAndVerified(command.getEmail(), true)
                .orElseThrow(() -> new EmailNotVerifiedException(command.getEmail()));

        // 3. 비밀번호 정책 검증
        passwordValidator.validate(command.getPassword());

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(command.getPassword());

        // 5. 회원 생성
        Member member = Member.createUser(
                command.getEmail(),
                encodedPassword,
                command.getName(),
                command.getNickname(),
                command.getPhone(),
                command.getLocation()
        );

        return memberRepository.save(member);
    }
}
