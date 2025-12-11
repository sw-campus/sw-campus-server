package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.EmailSendRequest;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.InvalidTokenException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AuthController - 이메일 인증 테스트")
class AuthControllerEmailTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private com.swcampus.domain.auth.TokenProvider tokenProvider;

    @MockitoBean
    private com.swcampus.api.config.CookieUtil cookieUtil;

    @Nested
    @DisplayName("POST /api/v1/auth/email/send - 인증 메일 발송")
    class SendVerificationEmail {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            EmailSendRequest request = new EmailSendRequest("user@example.com");
            doNothing().when(emailService).sendVerificationEmail(anyString());

            // when & then
            mockMvc.perform(post("/api/v1/auth/email/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("인증 메일이 발송되었습니다"));

            verify(emailService).sendVerificationEmail("user@example.com");
        }

        @Test
        @DisplayName("이메일 형식 오류 - 400")
        void fail_invalidEmail() throws Exception {
            // given
            EmailSendRequest request = new EmailSendRequest("invalid-email");

            // when & then
            mockMvc.perform(post("/api/v1/auth/email/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이메일 누락 - 400")
        void fail_missingEmail() throws Exception {
            // given
            EmailSendRequest request = new EmailSendRequest("");

            // when & then
            mockMvc.perform(post("/api/v1/auth/email/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이미 가입된 이메일 - 409")
        void fail_duplicateEmail() throws Exception {
            // given
            EmailSendRequest request = new EmailSendRequest("user@example.com");
            doThrow(new DuplicateEmailException())
                    .when(emailService).sendVerificationEmail(anyString());

            // when & then
            mockMvc.perform(post("/api/v1/auth/email/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/email/verify - 이메일 인증 (토큰 링크)")
    class VerifyEmail {

        @Test
        @DisplayName("성공 - HttpOnly 쿠키 설정 후 프론트엔드로 리다이렉트")
        void success() throws Exception {
            // given
            String token = "valid-token-uuid";
            String email = "user@example.com";
            when(emailService.verifyEmail(token)).thenReturn(email);
            
            ResponseCookie mockCookie = ResponseCookie.from("verifiedEmail", email)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(300)
                    .build();
            when(cookieUtil.createVerifiedEmailCookie(email)).thenReturn(mockCookie);

            // when & then
            mockMvc.perform(get("/api/v1/auth/email/verify")
                            .param("token", token))
                    .andExpect(status().isFound())  // 302 Redirect
                    .andExpect(header().string("Location", "http://localhost:3000/signup/personal?verified=true"))
                    .andExpect(header().exists("Set-Cookie"));

            verify(emailService).verifyEmail(token);
            verify(cookieUtil).createVerifiedEmailCookie(email);
        }

        @Test
        @DisplayName("유효하지 않은 토큰 - 에러 페이지로 리다이렉트")
        void fail_invalidToken() throws Exception {
            // given
            String token = "invalid-token";
            doThrow(new InvalidTokenException())
                    .when(emailService).verifyEmail(token);

            // when & then
            mockMvc.perform(get("/api/v1/auth/email/verify")
                            .param("token", token))
                    .andExpect(status().isFound())  // 302 Redirect
                    .andExpect(header().string("Location", "http://localhost:3000/signup/personal?error=invalid_token"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/email/verified - 인증된 이메일 조회")
    class GetVerifiedEmail {

        @Test
        @DisplayName("성공 - 쿠키에서 이메일 조회")
        void success() throws Exception {
            // given
            String email = "user@example.com";
            Cookie verifiedEmailCookie = new Cookie("verifiedEmail", email);

            // when & then
            mockMvc.perform(get("/api/v1/auth/email/verified")
                            .cookie(verifiedEmailCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email));
        }

        @Test
        @DisplayName("쿠키 없음 - 404")
        void fail_noCookie() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/auth/email/verified"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("빈 쿠키값 - 404")
        void fail_emptyCookie() throws Exception {
            // given
            Cookie emptyCookie = new Cookie("verifiedEmail", "");

            // when & then
            mockMvc.perform(get("/api/v1/auth/email/verified")
                            .cookie(emptyCookie))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/email/status - 인증 상태 확인")
    class CheckEmailStatus {

        @Test
        @DisplayName("인증 완료됨")
        void verified() throws Exception {
            // given
            String email = "user@example.com";
            when(emailService.isEmailVerified(email)).thenReturn(true);

            // when & then
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.verified").value(true));
        }

        @Test
        @DisplayName("인증되지 않음")
        void notVerified() throws Exception {
            // given
            String email = "user@example.com";
            when(emailService.isEmailVerified(email)).thenReturn(false);

            // when & then
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.verified").value(false));
        }
    }
}
