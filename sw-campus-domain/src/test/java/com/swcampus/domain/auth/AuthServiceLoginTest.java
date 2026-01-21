package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.InvalidCredentialsException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - 로그인/로그아웃 테스트")
class AuthServiceLoginTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("일반 회원 로그인에 성공한다")
        void loginUser() {
            // given
            String email = "user@example.com";
            String password = "Password1!";

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn(email);
            when(member.getPassword()).thenReturn("encodedPassword");
            when(member.getRole()).thenReturn(Role.USER);

            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
            when(tokenProvider.createAccessToken(1L, email, Role.USER)).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            LoginResult result = authService.login(email, password);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(result.getMember()).isEqualTo(member);
            assertThat(result.getOrganization()).isNull();
        }

        @Test
        @DisplayName("기관 회원 로그인에 성공하면 Organization 정보도 포함된다")
        void loginOrganization() {
            // given
            String email = "org@example.com";
            String password = "Password1!";

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn(email);
            when(member.getPassword()).thenReturn("encodedPassword");
            when(member.getRole()).thenReturn(Role.ORGANIZATION);
            when(member.getOrgId()).thenReturn(10L);

            Organization organization = mock(Organization.class);
            when(organization.getName()).thenReturn("테스트기관");
            when(organization.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);

            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
            when(tokenProvider.createAccessToken(1L, email, Role.ORGANIZATION)).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            LoginResult result = authService.login(email, password);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getOrganization()).isNotNull();
            assertThat(result.getOrganization().getName()).isEqualTo("테스트기관");
            assertThat(result.getOrganization().getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 실패한다")
        void loginEmailNotFound() {
            // given
            when(memberRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login("unknown@example.com", "password"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("비밀번호 불일치 시 로그인 실패한다")
        void loginWrongPassword() {
            // given
            Member member = mock(Member.class);
            when(member.getPassword()).thenReturn("encodedPassword");
            when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login("user@example.com", "wrongPassword"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("로그인 시 기존 Refresh Token이 삭제된다 (동시 로그인 제한)")
        void loginDeletesExistingRefreshToken() {
            // given
            String email = "user@example.com";
            String password = "Password1!";

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn(email);
            when(member.getPassword()).thenReturn("encodedPassword");
            when(member.getRole()).thenReturn(Role.USER);

            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
            when(tokenProvider.createAccessToken(any(), any(), any())).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            authService.login(email, password);

            // then
            verify(refreshTokenRepository).deleteByMemberId(1L);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("로그아웃 시 Refresh Token이 삭제된다")
        void logout() {
            // given
            Long memberId = 1L;

            // when
            authService.logout(memberId);

            // then
            verify(refreshTokenRepository).deleteByMemberId(memberId);
        }
    }
}
