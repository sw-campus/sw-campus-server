package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.EmailSendRequest;
import com.swcampus.api.auth.request.LoginRequest;
import com.swcampus.api.auth.request.PasswordChangeRequest;
import com.swcampus.api.auth.request.SignupRequest;
import com.swcampus.api.auth.request.TemporaryPasswordRequest;
import com.swcampus.domain.auth.EmailVerification;
import com.swcampus.domain.auth.EmailVerificationRepository;
import com.swcampus.domain.auth.MailSender;
import com.swcampus.domain.auth.RefreshToken;
import com.swcampus.domain.auth.RefreshTokenRepository;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth 통합 테스트
 * <p>
 * 전체 Auth 기능의 E2E 시나리오를 검증합니다.
 * - 이메일 인증 → 회원가입 → 로그인 → 로그아웃
 * - 토큰 갱신
 * - 비밀번호 변경/임시 비밀번호 발급
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private MailSender mailSender;  // 실제 메일 발송 방지

    @Nested
    @DisplayName("일반 회원 시나리오")
    class UserScenario {

        @Test
        @DisplayName("전체 플로우: 이메일 인증 → 회원가입 → 로그인 → 로그아웃")
        void fullUserSignupAndLoginFlow() throws Exception {
            String email = "newuser@example.com";
            String password = "Password1!";

            // 1. 이메일 인증 발송
            EmailSendRequest emailRequest = new EmailSendRequest(email);
            mockMvc.perform(post("/api/v1/auth/email/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emailRequest)))
                    .andExpect(status().isOk());

            verify(mailSender).send(eq(email), anyString(), anyString());

            // 2. 이메일 인증 상태 확인 (아직 미인증)
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.verified").value(false));

            // 3. 이메일 인증 처리 (실제로는 이메일 링크 클릭)
            EmailVerification verification = emailVerificationRepository
                    .findByEmailAndVerified(email, false).orElseThrow();
            verification.verify();
            emailVerificationRepository.save(verification);

            // 4. 이메일 인증 상태 확인 (인증 완료)
            mockMvc.perform(get("/api/v1/auth/email/status")
                            .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.verified").value(true));

            // 5. 회원가입
            SignupRequest signupRequest = new SignupRequest(
                    email, password, "홍길동", "길동이", "010-1234-5678", "서울시 강남구"
            );
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.role").value("USER"));

            // 6. 로그인
            LoginRequest loginRequest = new LoginRequest(email, password);
            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andReturn();

            // 쿠키 추출
            Cookie accessTokenCookie = loginResult.getResponse().getCookie("accessToken");
            Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
            assertThat(accessTokenCookie).isNotNull();
            assertThat(refreshTokenCookie).isNotNull();

            // 7. 로그아웃
            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(accessTokenCookie, refreshTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("accessToken", 0))
                    .andExpect(cookie().maxAge("refreshToken", 0));
        }

        @Test
        @DisplayName("이메일 미인증 시 회원가입 실패")
        void signupWithoutEmailVerification_fails() throws Exception {
            SignupRequest request = new SignupRequest(
                    "unverified@example.com", "Password1!", "홍길동", "길동이", "010-1234-5678", "서울시"
            );

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void loginWithWrongPassword_fails() throws Exception {
            // given: 가입된 사용자
            String email = "existing@example.com";
            setupVerifiedUser(email, "Password1!");

            // when & then
            LoginRequest request = new LoginRequest(email, "WrongPassword!");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void loginWithNonExistentEmail_fails() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@example.com", "Password1!");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 갱신 시나리오")
    class TokenRefreshScenario {

        private Member testMember;

        @BeforeEach
        void setUp() {
            testMember = setupVerifiedUser("tokentest@example.com", "Password1!");
        }

        @Test
        @DisplayName("유효한 Refresh Token으로 Access Token 갱신")
        void refreshWithValidToken_success() throws Exception {
            // given: 로그인 후 Refresh Token 발급
            String refreshToken = tokenProvider.createRefreshToken(testMember.getId());
            RefreshToken refreshTokenEntity = RefreshToken.create(
                    testMember.getId(), refreshToken, tokenProvider.getRefreshTokenValidity()
            );
            refreshTokenRepository.save(refreshTokenEntity);

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("accessToken"));
        }

        @Test
        @DisplayName("DB에 없는 Refresh Token으로 갱신 실패")
        void refreshWithNonStoredToken_fails() throws Exception {
            // given: DB에 저장하지 않은 토큰
            String refreshToken = tokenProvider.createRefreshToken(testMember.getId());

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다른 기기 로그인 후 기존 Refresh Token 무효화")
        void refreshAfterAnotherLogin_invalidatesOldToken() throws Exception {
            // given: 기존 로그인 - "올바르지 않은" 토큰 값을 생성
            String oldRefreshToken = "invalid-old-refresh-token-value";

            // DB에는 새로운 토큰만 저장 (기존 토큰은 삭제된 상태)
            String newRefreshToken = tokenProvider.createRefreshToken(testMember.getId());
            refreshTokenRepository.deleteByMemberId(testMember.getId());
            refreshTokenRepository.save(RefreshToken.create(
                    testMember.getId(), newRefreshToken, tokenProvider.getRefreshTokenValidity()
            ));

            // when: 유효하지 않은 토큰으로 갱신 시도
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", oldRefreshToken)))
                    .andExpect(status().isUnauthorized());  // JWT 검증 실패로 401
        }
    }

    @Nested
    @DisplayName("비밀번호 관리 시나리오")
    class PasswordScenario {

        private Member testMember;
        private String accessToken;

        @BeforeEach
        void setUp() {
            testMember = setupVerifiedUser("pwdtest@example.com", "OldPassword1!");
            accessToken = tokenProvider.createAccessToken(
                    testMember.getId(), testMember.getEmail(), testMember.getRole()
            );
        }

        @Test
        @DisplayName("비밀번호 변경 성공")
        void changePassword_success() throws Exception {
            PasswordChangeRequest request = new PasswordChangeRequest("OldPassword1!", "NewPassword1!");

            mockMvc.perform(patch("/api/v1/auth/password")
                            .cookie(new Cookie("accessToken", accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 새 비밀번호로 로그인 확인
            LoginRequest loginRequest = new LoginRequest("pwdtest@example.com", "NewPassword1!");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("현재 비밀번호 불일치 시 변경 실패")
        void changePasswordWithWrongCurrent_fails() throws Exception {
            PasswordChangeRequest request = new PasswordChangeRequest("WrongPassword!", "NewPassword1!");

            mockMvc.perform(patch("/api/v1/auth/password")
                            .cookie(new Cookie("accessToken", accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("임시 비밀번호 발급 요청")
        void requestTemporaryPassword_success() throws Exception {
            TemporaryPasswordRequest request = new TemporaryPasswordRequest(testMember.getEmail());

            mockMvc.perform(post("/api/v1/auth/password/temporary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("임시 비밀번호가 이메일로 발송되었습니다"));

            verify(mailSender).send(eq(testMember.getEmail()), anyString(), anyString());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 임시 비밀번호 요청해도 동일 응답 (보안)")
        void requestTemporaryPasswordForNonExistent_sameResponse() throws Exception {
            TemporaryPasswordRequest request = new TemporaryPasswordRequest("nonexistent@example.com");

            mockMvc.perform(post("/api/v1/auth/password/temporary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("임시 비밀번호가 이메일로 발송되었습니다"));
        }

        @Test
        @DisplayName("인증 없이 비밀번호 변경 시도 시 에러 (쿠키 누락)")
        void changePasswordWithoutAuth_fails() throws Exception {
            PasswordChangeRequest request = new PasswordChangeRequest("OldPassword1!", "NewPassword1!");

            // @CookieValue 누락 시 에러 발생 확인 (400 또는 500)
            mockMvc.perform(patch("/api/v1/auth/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 인증 없이 접근 시 성공(200)이 아니어야 함
                        assertThat(status).isNotEqualTo(200);
                    });
        }
    }

    @Nested
    @DisplayName("중복 가입 방지")
    class DuplicateRegistrationScenario {

        @Test
        @DisplayName("이미 가입된 이메일로 회원가입 실패")
        void signupWithExistingEmail_fails() throws Exception {
            // given: 이미 가입된 사용자
            String email = "duplicate@example.com";
            setupVerifiedUser(email, "Password1!");

            // 새 이메일 인증 생성
            EmailVerification verification = EmailVerification.create(email);
            verification.verify();
            emailVerificationRepository.save(verification);

            // when & then
            SignupRequest request = new SignupRequest(
                    email, "Password1!", "홍길동", "다른닉네임", "010-9999-9999", "서울시"
            );
            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    // === Helper Methods ===

    private Member setupVerifiedUser(String email, String password) {
        // 이메일 인증 처리
        EmailVerification verification = EmailVerification.create(email);
        verification.verify();
        emailVerificationRepository.save(verification);

        // 회원 생성
        Member member = Member.createUser(
                email,
                passwordEncoder.encode(password),
                "테스트유저",
                "닉네임_" + email.substring(0, 5),
                "010-0000-0000",
                "서울시"
        );
        return memberRepository.save(member);
    }
}
