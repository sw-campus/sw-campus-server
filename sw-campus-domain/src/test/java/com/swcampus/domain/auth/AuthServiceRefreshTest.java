package com.swcampus.domain.auth;

import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.auth.exception.TokenExpiredException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - 토큰 갱신 테스트")
class AuthServiceRefreshTest {

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

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                memberRepository,
                organizationRepository,
                emailVerificationRepository,
                refreshTokenRepository,
                passwordEncoder,
                passwordValidator,
                fileStorageService,
                tokenProvider
        );
    }

    @Nested
    @DisplayName("토큰 갱신 성공")
    class RefreshSuccess {

        @Test
        @DisplayName("유효한 Refresh Token으로 새 Access Token을 발급받는다")
        void refresh() {
            // given
            String refreshToken = "valid-refresh-token";
            Long memberId = 1L;

            RefreshToken storedToken = mock(RefreshToken.class);
            when(storedToken.getToken()).thenReturn(refreshToken);
            when(storedToken.isExpired()).thenReturn(false);

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(memberId);
            when(member.getEmail()).thenReturn("user@example.com");
            when(member.getRole()).thenReturn(Role.USER);

            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getMemberId(refreshToken)).thenReturn(memberId);
            when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.of(storedToken));
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(tokenProvider.createAccessToken(memberId, "user@example.com", Role.USER))
                    .thenReturn("new-access-token");

            // when
            String newAccessToken = authService.refresh(refreshToken);

            // then
            assertThat(newAccessToken).isEqualTo("new-access-token");
        }
    }

    @Nested
    @DisplayName("토큰 갱신 실패")
    class RefreshFailure {

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로 갱신 시 실패한다")
        void refresh_invalidToken() {
            // given
            String refreshToken = "invalid-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("DB에 없는 Refresh Token으로 갱신 시 실패한다")
        void refresh_tokenNotInDb() {
            // given
            String refreshToken = "valid-but-not-in-db";
            Long memberId = 1L;

            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getMemberId(refreshToken)).thenReturn(memberId);
            when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("토큰 값이 일치하지 않으면 갱신 실패 (다른 기기에서 로그인)")
        void refresh_tokenMismatch() {
            // given
            String refreshToken = "old-refresh-token";
            Long memberId = 1L;

            RefreshToken storedToken = mock(RefreshToken.class);
            when(storedToken.getToken()).thenReturn("new-refresh-token");  // 다른 토큰

            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getMemberId(refreshToken)).thenReturn(memberId);
            when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.of(storedToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("만료된 Refresh Token으로 갱신 시 실패하고 토큰이 삭제된다")
        void refresh_expiredToken() {
            // given
            String refreshToken = "expired-refresh-token";
            Long memberId = 1L;

            RefreshToken storedToken = mock(RefreshToken.class);
            when(storedToken.getToken()).thenReturn(refreshToken);
            when(storedToken.isExpired()).thenReturn(true);

            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getMemberId(refreshToken)).thenReturn(memberId);
            when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.of(storedToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(TokenExpiredException.class);

            verify(refreshTokenRepository).deleteByMemberId(memberId);
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 갱신 실패한다")
        void refresh_memberNotFound() {
            // given
            String refreshToken = "valid-refresh-token";
            Long memberId = 1L;

            RefreshToken storedToken = mock(RefreshToken.class);
            when(storedToken.getToken()).thenReturn(refreshToken);
            when(storedToken.isExpired()).thenReturn(false);

            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getMemberId(refreshToken)).thenReturn(memberId);
            when(refreshTokenRepository.findByMemberId(memberId)).thenReturn(Optional.of(storedToken));
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }
}
