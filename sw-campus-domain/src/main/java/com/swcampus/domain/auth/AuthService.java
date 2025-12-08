package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.CertificateRequiredException;
import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailNotVerifiedException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final FileStorageService fileStorageService;

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

    public OrganizationSignupResult signupOrganization(OrganizationSignupCommand command) {
        // 1. 재직증명서 확인
        if (command.getCertificateImage() == null || command.getCertificateImage().length == 0) {
            throw new CertificateRequiredException();
        }

        // 2. 중복 이메일 검증
        if (memberRepository.existsByEmail(command.getEmail())) {
            throw new DuplicateEmailException(command.getEmail());
        }

        // 3. 이메일 인증 여부 확인
        emailVerificationRepository.findByEmailAndVerified(command.getEmail(), true)
                .orElseThrow(() -> new EmailNotVerifiedException(command.getEmail()));

        // 4. 비밀번호 정책 검증
        passwordValidator.validate(command.getPassword());

        // 5. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(command.getPassword());

        // 6. 재직증명서 S3 업로드
        String certificateUrl = fileStorageService.upload(
                command.getCertificateImage(),
                command.getCertificateFileName(),
                command.getCertificateContentType()
        );

        // 7. 회원 생성 (ORGANIZATION 역할)
        Member member = Member.createOrganization(
                command.getEmail(),
                encodedPassword,
                command.getName(),
                command.getNickname(),
                command.getPhone(),
                command.getLocation()
        );
        Member savedMember = memberRepository.save(member);

        // 8. Organization 생성 (approvalStatus = PENDING)
        Organization organization = Organization.create(
                savedMember.getId(),
                command.getOrganizationName(),
                null,
                certificateUrl
        );
        Organization savedOrganization = organizationRepository.save(organization);

        // 9. Member에 orgId 연결
        savedMember.setOrgId(savedOrganization.getId());
        memberRepository.save(savedMember);

        return new OrganizationSignupResult(savedMember, savedOrganization);
    }
}
