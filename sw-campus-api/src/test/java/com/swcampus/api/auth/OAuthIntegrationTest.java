package com.swcampus.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.auth.request.OAuthCallbackRequest;
import com.swcampus.domain.auth.MailSender;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.oauth.OAuthClient;
import com.swcampus.domain.oauth.OAuthClientFactory;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.OAuthUserInfo;
import com.swcampus.domain.oauth.SocialAccountRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OAuth 통합 테스트
 * <p>
 * OAuth 로그인 시나리오를 검증합니다.
 * - 신규 사용자: 자동 회원가입 + 로그인
 * - 기존 사용자: 로그인
 * - 동일 이메일 계정 연동
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OAuth 통합 테스트")
class OAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @MockitoBean
    private OAuthClientFactory oAuthClientFactory;

    @MockitoBean
    private MailSender mailSender;  // 다른 테스트와 충돌 방지

    private OAuthClient mockOAuthClient;

    @BeforeEach
    void setUp() {
        mockOAuthClient = mock(OAuthClient.class);
    }

    // Helper method to create OAuthUserInfo using builder
    private OAuthUserInfo createOAuthUserInfo(OAuthProvider provider, String providerId, String email, String name) {
        return OAuthUserInfo.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name(name)
                .build();
    }

    @Nested
    @DisplayName("신규 사용자 OAuth 로그인")
    class NewUserOAuth {

        @Test
        @DisplayName("Google 로그인 - 신규 사용자 자동 회원가입")
        void googleLogin_newUser_autoRegister() throws Exception {
            // given
            String googleEmail = "newuser@gmail.com";
            String providerId = "google-provider-id-123";

            OAuthUserInfo userInfo = createOAuthUserInfo(
                    OAuthProvider.GOOGLE, providerId, googleEmail, "구글사용자"
            );

            when(oAuthClientFactory.getClient(OAuthProvider.GOOGLE)).thenReturn(mockOAuthClient);
            when(mockOAuthClient.getUserInfo("google-auth-code")).thenReturn(userInfo);

            // when
            OAuthCallbackRequest request = new OAuthCallbackRequest("google-auth-code");
            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(googleEmail))
                    .andExpect(jsonPath("$.name").value("구글사용자"))
                    .andExpect(cookie().exists("accessToken"))
                    .andExpect(cookie().exists("refreshToken"));

            // then: 회원이 생성되었는지 확인
            Member member = memberRepository.findByEmail(googleEmail).orElseThrow();
            assertThat(member.getPassword()).isNull();  // OAuth 사용자는 비밀번호 없음

            // 소셜 계정 연동 확인
            assertThat(socialAccountRepository.findByProviderAndProviderId(
                    OAuthProvider.GOOGLE, providerId
            )).isPresent();
        }

        @Test
        @DisplayName("GitHub 로그인 - 신규 사용자 자동 회원가입")
        void githubLogin_newUser_autoRegister() throws Exception {
            // given
            String githubEmail = "newuser@github.com";
            String providerId = "github-provider-id-456";

            OAuthUserInfo userInfo = createOAuthUserInfo(
                    OAuthProvider.GITHUB, providerId, githubEmail, "깃헙사용자"
            );

            when(oAuthClientFactory.getClient(OAuthProvider.GITHUB)).thenReturn(mockOAuthClient);
            when(mockOAuthClient.getUserInfo("github-auth-code")).thenReturn(userInfo);

            // when
            OAuthCallbackRequest request = new OAuthCallbackRequest("github-auth-code");
            mockMvc.perform(post("/api/v1/auth/oauth/github")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(githubEmail))
                    .andExpect(jsonPath("$.name").value("깃헙사용자"));

            // then
            assertThat(memberRepository.findByEmail(githubEmail)).isPresent();
        }
    }

    @Nested
    @DisplayName("기존 사용자 OAuth 로그인")
    class ExistingUserOAuth {

        @Test
        @DisplayName("이미 OAuth로 가입한 사용자 재로그인")
        void existingOAuthUser_login() throws Exception {
            // given: 이미 Google로 가입한 사용자
            String email = "existing@gmail.com";
            String providerId = "existing-google-id";

            // 첫 번째 로그인 (회원가입)
            OAuthUserInfo userInfo = createOAuthUserInfo(
                    OAuthProvider.GOOGLE, providerId, email, "기존사용자"
            );
            when(oAuthClientFactory.getClient(OAuthProvider.GOOGLE)).thenReturn(mockOAuthClient);
            when(mockOAuthClient.getUserInfo("first-code")).thenReturn(userInfo);

            OAuthCallbackRequest firstRequest = new OAuthCallbackRequest("first-code");
            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            // when: 두 번째 로그인 (재로그인)
            when(mockOAuthClient.getUserInfo("second-code")).thenReturn(userInfo);

            OAuthCallbackRequest secondRequest = new OAuthCallbackRequest("second-code");
            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email));

            // then: 회원은 하나만 존재
            assertThat(memberRepository.findByEmail(email)).isPresent();
        }

        @Test
        @DisplayName("동일 이메일로 다른 OAuth Provider 로그인 시 계정 연동")
        void sameEmail_differentProvider_linkAccount() throws Exception {
            // given: Google로 먼저 가입
            String email = "shared@example.com";

            OAuthUserInfo googleUserInfo = createOAuthUserInfo(
                    OAuthProvider.GOOGLE, "google-id", email, "공유계정"
            );
            when(oAuthClientFactory.getClient(OAuthProvider.GOOGLE)).thenReturn(mockOAuthClient);
            when(mockOAuthClient.getUserInfo("google-code")).thenReturn(googleUserInfo);

            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OAuthCallbackRequest("google-code"))))
                    .andExpect(status().isOk());

            Long memberId = memberRepository.findByEmail(email).orElseThrow().getId();

            // when: GitHub로 동일 이메일로 로그인
            OAuthUserInfo githubUserInfo = createOAuthUserInfo(
                    OAuthProvider.GITHUB, "github-id", email, "공유계정"
            );
            when(oAuthClientFactory.getClient(OAuthProvider.GITHUB)).thenReturn(mockOAuthClient);
            when(mockOAuthClient.getUserInfo("github-code")).thenReturn(githubUserInfo);

            mockMvc.perform(post("/api/v1/auth/oauth/github")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OAuthCallbackRequest("github-code"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email));

            // then: 회원은 하나, 소셜 계정은 두 개
            assertThat(memberRepository.findByEmail(email)).isPresent();
            assertThat(socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-id")).isPresent();
            assertThat(socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GITHUB, "github-id")).isPresent();

            // 모든 소셜 계정이 같은 회원에 연결
            Long googleMemberId = socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-id")
                    .orElseThrow().getMemberId();
            Long githubMemberId = socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GITHUB, "github-id")
                    .orElseThrow().getMemberId();
            assertThat(googleMemberId).isEqualTo(githubMemberId).isEqualTo(memberId);
        }
    }

    @Nested
    @DisplayName("OAuth 로그인 후 로그아웃")
    class OAuthLogout {

        @Test
        @DisplayName("OAuth 로그인 후 로그아웃")
        void oauthLoginThenLogout() throws Exception {
            // given
            OAuthUserInfo userInfo = createOAuthUserInfo(
                    OAuthProvider.GOOGLE, "logout-test-id", "logout@gmail.com", "로그아웃테스트"
            );
            when(oAuthClientFactory.getClient(OAuthProvider.GOOGLE)).thenReturn(mockOAuthClient);
            when(mockOAuthClient.getUserInfo("auth-code")).thenReturn(userInfo);

            // OAuth 로그인
            OAuthCallbackRequest request = new OAuthCallbackRequest("auth-code");
            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            Cookie accessTokenCookie = loginResult.getResponse().getCookie("accessToken");
            Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
            assertThat(accessTokenCookie).isNotNull();
            assertThat(refreshTokenCookie).isNotNull();

            // when: 로그아웃
            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(accessTokenCookie, refreshTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("accessToken", 0))
                    .andExpect(cookie().maxAge("refreshToken", 0));
        }
    }

    @Nested
    @DisplayName("OAuth 에러 케이스")
    class OAuthErrorCases {

        @Test
        @DisplayName("빈 authorization code로 요청 시 400")
        void emptyCode_returns400() throws Exception {
            OAuthCallbackRequest request = new OAuthCallbackRequest("");

            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("지원하지 않는 provider로 요청 시 에러")
        void unsupportedProvider_returnsError() throws Exception {
            OAuthCallbackRequest request = new OAuthCallbackRequest("some-code");

            // facebook은 지원하지 않는 provider - 400 또는 500
            mockMvc.perform(post("/api/v1/auth/oauth/facebook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 성공(200)이 아니어야 함
                        assertThat(status).isNotEqualTo(200);
                        // 에러 응답 (400 또는 500)
                        assertThat(status).isGreaterThanOrEqualTo(400);
                    });
        }
    }
}
