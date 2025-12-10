package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.OAuthCallbackRequest;
import com.swcampus.api.config.CookieUtil;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.oauth.OAuthLoginResult;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.OAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = OAuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("OAuthController 테스트")
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OAuthService oAuthService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Nested
    @DisplayName("POST /api/v1/auth/oauth/{provider}")
    class OAuthLogin {

        @Test
        @DisplayName("Google 로그인 성공")
        void googleLogin() throws Exception {
            // given
            OAuthCallbackRequest request = new OAuthCallbackRequest("google-auth-code");

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn("user@gmail.com");
            when(member.getName()).thenReturn("홍길동");
            when(member.getNickname()).thenReturn("사용자_a1b2c3d4");
            when(member.getRole()).thenReturn(Role.USER);

            OAuthLoginResult result = new OAuthLoginResult("access-token", "refresh-token", member);

            when(oAuthService.loginOrRegister(eq(OAuthProvider.GOOGLE), eq("google-auth-code"))).thenReturn(result);
            when(tokenProvider.getAccessTokenValidity()).thenReturn(3600L);
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);
            when(cookieUtil.createAccessTokenCookie(any(), anyLong()))
                    .thenReturn(ResponseCookie.from("accessToken", "access-token").build());
            when(cookieUtil.createRefreshTokenCookie(any(), anyLong()))
                    .thenReturn(ResponseCookie.from("refreshToken", "refresh-token").build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1))
                    .andExpect(jsonPath("$.email").value("user@gmail.com"))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.nickname").value("사용자_a1b2c3d4"))
                    .andExpect(header().exists("Set-Cookie"));
        }

        @Test
        @DisplayName("GitHub 로그인 성공")
        void githubLogin() throws Exception {
            // given
            OAuthCallbackRequest request = new OAuthCallbackRequest("github-auth-code");

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn("user@github.com");
            when(member.getName()).thenReturn("깃헙유저");
            when(member.getNickname()).thenReturn("사용자_b2c3d4e5");
            when(member.getRole()).thenReturn(Role.USER);

            OAuthLoginResult result = new OAuthLoginResult("access-token", "refresh-token", member);

            when(oAuthService.loginOrRegister(eq(OAuthProvider.GITHUB), eq("github-auth-code"))).thenReturn(result);
            when(tokenProvider.getAccessTokenValidity()).thenReturn(3600L);
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);
            when(cookieUtil.createAccessTokenCookie(any(), anyLong()))
                    .thenReturn(ResponseCookie.from("accessToken", "access-token").build());
            when(cookieUtil.createRefreshTokenCookie(any(), anyLong()))
                    .thenReturn(ResponseCookie.from("refreshToken", "refresh-token").build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/oauth/github")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1))
                    .andExpect(jsonPath("$.nickname").value("사용자_b2c3d4e5"));
        }

        @Test
        @DisplayName("인증 코드가 비어있으면 400 에러")
        void emptyCode() throws Exception {
            // given
            OAuthCallbackRequest request = new OAuthCallbackRequest("");

            // when & then
            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("지원하지 않는 provider면 500 에러")
        void unsupportedProvider() throws Exception {
            // given
            OAuthCallbackRequest request = new OAuthCallbackRequest("some-code");

            // when & then
            mockMvc.perform(post("/api/v1/auth/oauth/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
