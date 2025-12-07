package com.swcampus.domain.auth;

import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenProvider 테스트")
class TokenProviderTest {

    private TokenProvider tokenProvider;

    private static final String TEST_SECRET = "test-secret-key-for-testing-purpose-only-32bytes!!";
    private static final long ACCESS_TOKEN_VALIDITY = 3600L;  // 1시간
    private static final long REFRESH_TOKEN_VALIDITY = 604800L; // 7일

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(TEST_SECRET, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY);
    }

    @Nested
    @DisplayName("Access Token 생성")
    class CreateAccessToken {

        @Test
        @DisplayName("Access Token을 생성할 수 있다")
        void createAccessToken() {
            // given
            Long memberId = 1L;
            String email = "user@example.com";
            Role role = Role.USER;

            // when
            String token = tokenProvider.createAccessToken(memberId, email, role);

            // then
            assertThat(token).isNotBlank();
            assertThat(tokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("생성된 Access Token에서 memberId를 추출할 수 있다")
        void extractMemberIdFromAccessToken() {
            // given
            Long memberId = 1L;
            String token = tokenProvider.createAccessToken(memberId, "user@example.com", Role.USER);

            // when
            Long extractedMemberId = tokenProvider.getMemberId(token);

            // then
            assertThat(extractedMemberId).isEqualTo(memberId);
        }

        @Test
        @DisplayName("생성된 Access Token에서 email을 추출할 수 있다")
        void extractEmailFromAccessToken() {
            // given
            String email = "user@example.com";
            String token = tokenProvider.createAccessToken(1L, email, Role.USER);

            // when
            String extractedEmail = tokenProvider.getEmail(token);

            // then
            assertThat(extractedEmail).isEqualTo(email);
        }

        @Test
        @DisplayName("생성된 Access Token에서 role을 추출할 수 있다")
        void extractRoleFromAccessToken() {
            // given
            Role role = Role.ADMIN;
            String token = tokenProvider.createAccessToken(1L, "admin@example.com", role);

            // when
            Role extractedRole = tokenProvider.getRole(token);

            // then
            assertThat(extractedRole).isEqualTo(role);
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    class CreateRefreshToken {

        @Test
        @DisplayName("Refresh Token을 생성할 수 있다")
        void createRefreshToken() {
            // given
            Long memberId = 1L;

            // when
            String token = tokenProvider.createRefreshToken(memberId);

            // then
            assertThat(token).isNotBlank();
            assertThat(tokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("생성된 Refresh Token에서 memberId를 추출할 수 있다")
        void extractMemberIdFromRefreshToken() {
            // given
            Long memberId = 1L;
            String token = tokenProvider.createRefreshToken(memberId);

            // when
            Long extractedMemberId = tokenProvider.getMemberId(token);

            // then
            assertThat(extractedMemberId).isEqualTo(memberId);
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class ValidateToken {

        @Test
        @DisplayName("유효한 토큰은 검증에 성공한다")
        void validateValidToken() {
            // given
            String token = tokenProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            assertThat(tokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 검증에 실패한다")
        void validateExpiredToken() {
            // given
            TokenProvider shortLivedProvider = new TokenProvider(TEST_SECRET, 0L, 0L);
            String token = shortLivedProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            assertThat(tokenProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰은 검증에 실패한다")
        void validateMalformedToken() {
            // given
            String invalidToken = "invalid.token.here";

            // when & then
            assertThat(tokenProvider.validateToken(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("null 토큰은 검증에 실패한다")
        void validateNullToken() {
            // when & then
            assertThat(tokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰은 검증에 실패한다")
        void validateEmptyToken() {
            // when & then
            assertThat(tokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("다른 secret으로 생성된 토큰은 검증에 실패한다")
        void validateTokenWithDifferentSecret() {
            // given
            TokenProvider otherProvider = new TokenProvider(
                    "different-secret-key-for-testing-32bytes!!", 
                    ACCESS_TOKEN_VALIDITY, 
                    REFRESH_TOKEN_VALIDITY
            );
            String token = otherProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            assertThat(tokenProvider.validateToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 유효시간 조회")
    class GetTokenValidity {

        @Test
        @DisplayName("Access Token 유효시간을 조회할 수 있다")
        void getAccessTokenValidity() {
            // when
            long validity = tokenProvider.getAccessTokenValidity();

            // then
            assertThat(validity).isEqualTo(ACCESS_TOKEN_VALIDITY);
        }

        @Test
        @DisplayName("Refresh Token 유효시간을 조회할 수 있다")
        void getRefreshTokenValidity() {
            // when
            long validity = tokenProvider.getRefreshTokenValidity();

            // then
            assertThat(validity).isEqualTo(REFRESH_TOKEN_VALIDITY);
        }
    }
}
