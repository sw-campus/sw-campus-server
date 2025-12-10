package com.swcampus.api.auth;

import com.swcampus.api.config.CookieUtil;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.auth.exception.TokenExpiredException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AuthController - 토큰 갱신 테스트")
class AuthControllerRefreshTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("토큰 갱신 성공 시 새 Access Token 쿠키를 반환한다")
        void refresh_success() throws Exception {
            // given
            String refreshToken = "valid-refresh-token";
            String newAccessToken = "new-access-token";

            when(authService.refresh(refreshToken)).thenReturn(newAccessToken);
            when(tokenProvider.getAccessTokenValidity()).thenReturn(3600L);
            when(cookieUtil.createAccessTokenCookie(newAccessToken, 3600L))
                    .thenReturn(ResponseCookie.from("accessToken", newAccessToken).build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("Refresh Token이 없으면 401 반환")
        void refresh_noToken() throws Exception {
            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token이면 401 반환")
        void refresh_invalidToken() throws Exception {
            // given
            String refreshToken = "invalid-token";
            when(authService.refresh(refreshToken)).thenThrow(new InvalidTokenException());

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("만료된 Refresh Token이면 401 반환")
        void refresh_expiredToken() throws Exception {
            // given
            String refreshToken = "expired-token";
            when(authService.refresh(refreshToken)).thenThrow(new TokenExpiredException());

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
