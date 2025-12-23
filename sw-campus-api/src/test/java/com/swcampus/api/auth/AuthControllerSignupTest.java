package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.SignupRequest;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.auth.exception.DuplicateEmailException;
import com.swcampus.domain.auth.exception.EmailNotVerifiedException;
import com.swcampus.domain.auth.exception.InvalidPasswordException;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.OrganizationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.ResponseCookie;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AuthController - 회원가입 테스트")
class AuthControllerSignupTest {

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

    @MockitoBean
    private OrganizationService organizationService;

    @Nested
    @DisplayName("POST /api/v1/auth/signup - 회원가입")
    class Signup {

        @Test
        @DisplayName("성공 - 201 Created + 이메일 쿠키 삭제")
        void success() throws Exception {
            // given
            SignupRequest request = SignupRequest.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            Member member = Member.of(
                    1L,
                    "user@example.com",
                    "encodedPassword",
                    "홍길동",
                    "길동이",
                    "010-1234-5678",
                    Role.USER,
                    null,
                    "서울시 강남구",
                    null,
                    null
            );
            when(authService.signup(any())).thenReturn(member);
            
            ResponseCookie deleteCookie = ResponseCookie.from("verifiedEmail", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(0)
                    .build();
            when(cookieUtil.deleteVerifiedEmailCookie()).thenReturn(deleteCookie);

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.user_id").value(1))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.nickname").value("길동이"))
                    .andExpect(jsonPath("$.role").value("USER"));
            
            verify(cookieUtil).deleteVerifiedEmailCookie();
        }

        @Test
        @DisplayName("이메일 형식 오류 - 400")
        void fail_invalidEmail() throws Exception {
            // given
            SignupRequest request = SignupRequest.builder()
                    .email("invalid-email")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("필수 필드 누락 - 400")
        void fail_missingRequired() throws Exception {
            // given
            SignupRequest request = SignupRequest.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("")
                    .nickname("")
                    .build();

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("중복 이메일 - 409")
        void fail_duplicateEmail() throws Exception {
            // given
            SignupRequest request = SignupRequest.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(authService.signup(any())).thenThrow(new DuplicateEmailException("user@example.com"));

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("이메일 미인증 - 400")
        void fail_emailNotVerified() throws Exception {
            // given
            SignupRequest request = SignupRequest.builder()
                    .email("user@example.com")
                    .password("Password1!")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(authService.signup(any())).thenThrow(new EmailNotVerifiedException("user@example.com"));

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호 정책 위반 - 400")
        void fail_invalidPassword() throws Exception {
            // given
            SignupRequest request = SignupRequest.builder()
                    .email("user@example.com")
                    .password("short")
                    .name("홍길동")
                    .nickname("길동이")
                    .phone("010-1234-5678")
                    .location("서울시 강남구")
                    .build();

            when(authService.signup(any())).thenThrow(new InvalidPasswordException("비밀번호는 8자 이상이어야 합니다"));

            // when & then
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
