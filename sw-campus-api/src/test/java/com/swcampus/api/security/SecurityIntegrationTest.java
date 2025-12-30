package com.swcampus.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.EmailSendRequest;
import com.swcampus.domain.auth.MailSender;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
}
