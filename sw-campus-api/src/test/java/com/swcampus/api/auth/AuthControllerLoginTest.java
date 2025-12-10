package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.LoginRequest;
import com.swcampus.api.config.CookieUtil;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.auth.LoginResult;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.auth.exception.InvalidCredentialsException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.ApprovalStatus;
import com.swcampus.domain.organization.Organization;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AuthController - 로그인/로그아웃 테스트")
class AuthControllerLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("일반 회원 로그인 성공")
        void loginUser() throws Exception {
            // given
            LoginRequest request = new LoginRequest("user@example.com", "Password1!");

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn("user@example.com");
            when(member.getName()).thenReturn("홍길동");
            when(member.getNickname()).thenReturn("길동이");
            when(member.getRole()).thenReturn(Role.USER);

            LoginResult result = new LoginResult("access-token", "refresh-token", member);
            when(authService.login(request.getEmail(), request.getPassword())).thenReturn(result);

            when(tokenProvider.getAccessTokenValidity()).thenReturn(3600L);
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            when(cookieUtil.createAccessTokenCookie("access-token", 3600L))
                    .thenReturn(ResponseCookie.from("accessToken", "access-token").build());
            when(cookieUtil.createRefreshTokenCookie("refresh-token", 86400L))
                    .thenReturn(ResponseCookie.from("refreshToken", "refresh-token").build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.organizationId").doesNotExist());
        }

        @Test
        @DisplayName("기관 회원 로그인 성공 - Organization 정보 포함")
        void loginOrganization() throws Exception {
            // given
            LoginRequest request = new LoginRequest("org@example.com", "Password1!");

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn("org@example.com");
            when(member.getName()).thenReturn("김기관");
            when(member.getNickname()).thenReturn("기관담당자");
            when(member.getRole()).thenReturn(Role.ORGANIZATION);

            Organization organization = mock(Organization.class);
            when(organization.getId()).thenReturn(10L);
            when(organization.getName()).thenReturn("테스트교육기관");
            when(organization.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);

            LoginResult result = new LoginResult("access-token", "refresh-token", member, organization);
            when(authService.login(request.getEmail(), request.getPassword())).thenReturn(result);

            when(tokenProvider.getAccessTokenValidity()).thenReturn(3600L);
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            when(cookieUtil.createAccessTokenCookie("access-token", 3600L))
                    .thenReturn(ResponseCookie.from("accessToken", "access-token").build());
            when(cookieUtil.createRefreshTokenCookie("refresh-token", 86400L))
                    .thenReturn(ResponseCookie.from("refreshToken", "refresh-token").build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.role").value("ORGANIZATION"))
                    .andExpect(jsonPath("$.organizationId").value(10))
                    .andExpect(jsonPath("$.organizationName").value("테스트교육기관"))
                    .andExpect(jsonPath("$.approvalStatus").value("PENDING"));
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 자격 증명")
        void loginFailed() throws Exception {
            // given
            LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");
            when(authService.login(request.getEmail(), request.getPassword()))
                    .thenThrow(new InvalidCredentialsException());

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("로그인 실패 - 이메일 형식 오류")
        void loginInvalidEmail() throws Exception {
            // given
            LoginRequest request = new LoginRequest("invalid-email", "Password1!");

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 누락")
        void loginMissingPassword() throws Exception {
            // given
            LoginRequest request = new LoginRequest("user@example.com", "");

            // when & then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {

        @Test
        @DisplayName("로그아웃 성공")
        void logout() throws Exception {
            // given
            when(tokenProvider.validateToken("valid-token")).thenReturn(true);
            when(tokenProvider.getMemberId("valid-token")).thenReturn(1L);
            when(cookieUtil.deleteAccessTokenCookie())
                    .thenReturn(ResponseCookie.from("accessToken", "").maxAge(0).build());
            when(cookieUtil.deleteRefreshTokenCookie())
                    .thenReturn(ResponseCookie.from("refreshToken", "").maxAge(0).build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(new Cookie("accessToken", "valid-token")))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"));

            verify(authService).logout(1L);
        }

        @Test
        @DisplayName("로그아웃 성공 - 토큰 없이도 성공")
        void logoutWithoutToken() throws Exception {
            // given
            when(cookieUtil.deleteAccessTokenCookie())
                    .thenReturn(ResponseCookie.from("accessToken", "").maxAge(0).build());
            when(cookieUtil.deleteRefreshTokenCookie())
                    .thenReturn(ResponseCookie.from("refreshToken", "").maxAge(0).build());

            // when & then
            mockMvc.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isOk());
        }
    }
}
