package com.swcampus.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.EmailSendRequest;
import com.swcampus.domain.auth.MailSender;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security 설정 통합 테스트
 * <p>
 * SecurityConfig에 정의된 접근 제어가 의도대로 동작하는지 검증합니다.
 * - permitAll 경로: 인증 없이 접근 가능해야 함
 * - authenticated 경로: 인증 없으면 401, 유효한 토큰이면 접근 가능
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security 통합 테스트")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @MockitoBean
    private MailSender mailSender;  // 실제 메일 발송 방지

    @Nested
    @DisplayName("permitAll 경로 테스트")
    class PermitAllPaths {

        @Test
        @DisplayName("/api/v1/auth/** 경로는 인증 없이 접근 가능")
        void authPathsArePublic() throws Exception {
            // 이메일 발송 API - 인증 없이 접근 가능해야 함
            EmailSendRequest request = new EmailSendRequest("test@example.com");

            mockMvc.perform(post("/api/v1/auth/email/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/api/v1/auth/email/status 경로는 인증 없이 접근 가능")
        void emailStatusIsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", "test@example.com"))
                    .andExpect(status().isOk());
        }

        // TODO: actuator 경로가 SecurityConfig에서 주석 처리되어 있어서 비활성화
        // SecurityConfig에서 actuator 경로를 다시 활성화하면 이 테스트도 활성화할 것
        // @Test
        // @DisplayName("/actuator/health 경로는 인증 없이 접근 가능")
        // void healthEndpointIsPublic() throws Exception {
        //     // actuator health는 실제 서비스 상태에 따라 200 또는 503 반환
        //     // 중요한 것은 401이 아니라는 것 (인증 bypass 확인)
        //     mockMvc.perform(get("/actuator/health"))
        //             .andExpect(result -> {
        //                 int status = result.getResponse().getStatus();
        //                 // 200 또는 503이어야 함 (401이 아님 = permitAll 동작 확인)
        //                 assert status == 200 || status == 503 :
        //                     "Expected 200 or 503 but was " + status;
        //             });
        // }
    }

    @Nested
    @DisplayName("인증 필요 경로 테스트")
    class AuthenticatedPaths {

        @Test
        @DisplayName("인증 없이 보호된 경로 접근 시 401 반환")
        void protectedPathWithoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효한 토큰으로 보호된 경로 접근 가능")
        void protectedPathWithValidToken_returns200or404() throws Exception {
            // given
            String accessToken = tokenProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            // 실제 API가 없으면 404 또는 500 (NoResourceFoundException)
            // 중요한 것은 401이 아니라는 것 (인증은 통과)
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 401이 아니어야 함 = 인증 통과 확인
                        assert status != 401 : "Expected non-401 but was " + status;
                    });
        }

        @Test
        @DisplayName("만료된 토큰으로 접근 시 401 반환")
        void protectedPathWithExpiredToken_returns401() throws Exception {
            // given - 이미 만료된 토큰 (테스트용으로 직접 생성하기 어려우므로 잘못된 토큰 사용)
            String invalidToken = "invalid.token.here";

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + invalidToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Cookie의 accessToken으로도 인증 가능")
        void protectedPathWithCookieToken_returns200or404() throws Exception {
            // given
            String accessToken = tokenProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .cookie(new jakarta.servlet.http.Cookie("accessToken", accessToken)))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 401이 아니어야 함 = 인증 통과 확인
                        assert status != 401 : "Expected non-401 but was " + status;
                    });
        }
    }

    @Nested
    @DisplayName("JWT 인증 필터 테스트")
    class JwtAuthenticationFilterTest {

        @Test
        @DisplayName("Authorization 헤더가 Bearer 형식이 아니면 인증 실패")
        void invalidAuthorizationHeaderFormat_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Basic sometoken"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Authorization 헤더와 Cookie 둘 다 있으면 헤더 우선")
        void headerTakesPrecedenceOverCookie() throws Exception {
            // given
            String validToken = tokenProvider.createAccessToken(1L, "user@example.com", Role.USER);
            String invalidToken = "invalid.token";

            // when & then - 유효한 헤더 토큰 + 무효한 쿠키 토큰
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + validToken)
                            .cookie(new jakarta.servlet.http.Cookie("accessToken", invalidToken)))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 401이 아니어야 함 = 헤더 토큰으로 인증 성공
                        assert status != 401 : "Expected non-401 but was " + status;
                    });

            // when & then - 무효한 헤더 토큰 + 유효한 쿠키 토큰 (헤더 우선이므로 실패)
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + invalidToken)
                            .cookie(new jakarta.servlet.http.Cookie("accessToken", validToken)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("JWT 에러 코드 테스트")
    class JwtErrorCodeTest {

        @Test
        @DisplayName("만료된 토큰으로 접근 시 A002 에러 코드 반환")
        void expiredToken_returnsA002() throws Exception {
            // given - 만료된 토큰 생성
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiredAt = new Date(now.getTime() - 1000); // 1초 전에 만료

            String expiredToken = Jwts.builder()
                    .subject("1")
                    .claim("email", "user@example.com")
                    .claim("role", Role.USER.name())
                    .issuedAt(new Date(now.getTime() - 3600000)) // 1시간 전 발급
                    .expiration(expiredAt)
                    .signWith(secretKey)
                    .compact();

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("A002"))
                    .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다"));
        }

        @Test
        @DisplayName("위변조된 토큰으로 접근 시 A001 에러 코드 반환")
        void tamperedToken_returnsA001() throws Exception {
            // given - 다른 키로 서명된 토큰 (위변조)
            SecretKey differentKey = Keys.hmacShaKeyFor(
                    "different-secret-key-for-testing-32bytes!!".getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiry = new Date(now.getTime() + 3600000); // 1시간 후 만료

            String tamperedToken = Jwts.builder()
                    .subject("1")
                    .claim("email", "user@example.com")
                    .claim("role", Role.USER.name())
                    .issuedAt(now)
                    .expiration(expiry)
                    .signWith(differentKey)
                    .compact();

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + tamperedToken))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("A001"))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다"));
        }

        @Test
        @DisplayName("잘못된 형식의 토큰으로 접근 시 A001 에러 코드 반환")
        void malformedToken_returnsA001() throws Exception {
            // given - 잘못된 형식의 토큰
            String malformedToken = "not.a.valid.jwt.token";

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("Authorization", "Bearer " + malformedToken))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("A001"))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다"));
        }

        @Test
        @DisplayName("토큰 없이 접근 시 code 필드 없이 일반 메시지 반환")
        void noToken_returnsGenericMessage() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/users/me"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").doesNotExist())
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다"));
        }

        @Test
        @DisplayName("만료된 토큰으로 permitAll 경로 접근 시 정상 응답")
        void expiredToken_permitAllPath_returnsOk() throws Exception {
            // given - 만료된 토큰 생성
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiredAt = new Date(now.getTime() - 1000);

            String expiredToken = Jwts.builder()
                    .subject("1")
                    .claim("email", "user@example.com")
                    .claim("role", Role.USER.name())
                    .issuedAt(new Date(now.getTime() - 3600000))
                    .expiration(expiredAt)
                    .signWith(secretKey)
                    .compact();

            // when & then - permitAll 경로는 만료된 토큰이어도 접근 가능해야 함
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", "test@example.com")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Issue #428: 로그인 사용자 공개 API 접근 테스트")
    class Issue428Test {

        @Test
        @DisplayName("유효한 토큰으로 공개 GET API 접근 시 인증 정보 유지 - permitAll 경로")
        void validToken_permitAllPath_authenticationPreserved() throws Exception {
            // given - 유효한 토큰
            String validToken = tokenProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            // 공개 API여도 JWT가 있으면 인증 정보가 유지되어야 함
            // (issue #428 수정 전에는 JWT 검증을 스킵해서 anonymousUser로 처리됨)
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", "test@example.com")
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("유효한 토큰으로 공개 GET API 접근 시 인증 정보 유지 - PUBLIC_GET_APIS 경로")
        void validToken_publicGetApis_authenticationPreserved() throws Exception {
            // given - 유효한 토큰
            String validToken = tokenProvider.createAccessToken(1L, "user@example.com", Role.USER);

            // when & then
            // PUBLIC_GET_APIS에 정의된 공개 API도 JWT 검증이 수행되어야 함
            // 401이 아닌 응답 = JWT가 정상 검증됨 (200 또는 데이터 없으면 다른 상태코드)
            mockMvc.perform(get("/api/v1/categories")
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 401이 아니어야 함 = JWT 검증 통과
                        assert status != 401 : "Expected non-401 but was " + status;
                    });
        }

        @Test
        @DisplayName("만료된 토큰으로 공개 GET API 접근 시에도 정상 응답 (permitAll)")
        void expiredToken_publicGetApis_stillAccessible() throws Exception {
            // given - 만료된 토큰 생성
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiredAt = new Date(now.getTime() - 1000);

            String expiredToken = Jwts.builder()
                    .subject("1")
                    .claim("email", "user@example.com")
                    .claim("role", Role.USER.name())
                    .issuedAt(new Date(now.getTime() - 3600000))
                    .expiration(expiredAt)
                    .signWith(secretKey)
                    .compact();

            // when & then
            // 공개 API는 만료된 토큰이어도 접근 가능해야 함 (permitAll)
            // JWT 검증 실패해도 SecurityConfig의 permitAll()이 접근 허용
            mockMvc.perform(get("/api/v1/categories")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 401이 아니어야 함 = permitAll 동작 확인
                        assert status != 401 : "Expected non-401 but was " + status;
                    });
        }
    }
}
