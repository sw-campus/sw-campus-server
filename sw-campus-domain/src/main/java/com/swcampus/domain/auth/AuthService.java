package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.CertificateRequiredException;
import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.DuplicateOrganizationMemberException;
import com.swcampus.domain.auth.exception.EmailNotVerifiedException;
import com.swcampus.domain.auth.exception.InvalidCredentialsException;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.auth.exception.TokenExpiredException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final FileStorageService fileStorageService;
    private final TokenProvider tokenProvider;

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
                "organizations",
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

        Organization organization;

        if (command.getOrganizationId() != null) {
            // 기존 기관 선택
            // 중복 가입 체크: 이미 다른 사용자가 연결된 기관인지 확인
            if (memberRepository.existsByOrgId(command.getOrganizationId())) {
                throw new DuplicateOrganizationMemberException(command.getOrganizationId());
            }

            // 기존 기관 조회
            organization = organizationRepository.findById(command.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("해당 기관을 찾을 수 없습니다: " + command.getOrganizationId()));

            // 재직증명서 URL 업데이트
            organization.updateCertificateUrl(certificateUrl);
            organization = organizationRepository.save(organization);

            // Member에 orgId 연결
            member.setOrgId(organization.getId());
            Member savedMember = memberRepository.save(member);

            return new OrganizationSignupResult(savedMember, organization);
        } else {
            // 신규 기관 생성
            Member savedMember = memberRepository.save(member);

            // Organization 생성 (approvalStatus = PENDING)
            organization = Organization.create(
                    savedMember.getId(),
                    command.getOrganizationName(),
                    null,
                    certificateUrl
            );
            Organization savedOrganization = organizationRepository.save(organization);

            // Member에 orgId 연결
            savedMember.setOrgId(savedOrganization.getId());
            memberRepository.save(savedMember);

            return new OrganizationSignupResult(savedMember, savedOrganization);
        }
    }

    public LoginResult login(String email, String password) {
        // 1. 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 3. 기존 Refresh Token 삭제 (동시 로그인 제한)
        refreshTokenRepository.deleteByMemberId(member.getId());

        // 4. 토큰 생성
        String accessToken = tokenProvider.createAccessToken(
                member.getId(), member.getEmail(), member.getRole());
        String refreshToken = tokenProvider.createRefreshToken(member.getId());

        // 5. Refresh Token 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(
                member.getId(),
                refreshToken,
                tokenProvider.getRefreshTokenValidity()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        // 6. ORGANIZATION인 경우 Organization 정보 조회
        Organization organization = null;
        if (member.getRole() == Role.ORGANIZATION && member.getOrgId() != null) {
            organization = organizationRepository.findById(member.getOrgId()).orElse(null);
        }

        return new LoginResult(accessToken, refreshToken, member, organization);
    }

    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    public String refresh(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        // 2. 토큰에서 사용자 ID 추출
        Long memberId = tokenProvider.getMemberId(refreshToken);

        // 3. DB에서 저장된 Refresh Token 조회
        RefreshToken storedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(InvalidTokenException::new);

        // 4. 토큰 값 일치 확인 (동시 로그인 제한)
        if (!storedToken.getToken().equals(refreshToken)) {
            throw new InvalidTokenException();
        }

        // 5. 만료 확인
        if (storedToken.isExpired()) {
            refreshTokenRepository.deleteByMemberId(memberId);
            throw new TokenExpiredException();
        }

        // 6. 사용자 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidTokenException::new);

        // 7. 새 Access Token 발급
        return tokenProvider.createAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );
    }
}
