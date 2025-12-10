package com.swcampus.domain.oauth;

import com.swcampus.domain.auth.RefreshTokenRepository;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthService 테스트")
class OAuthServiceTest {

    @Mock
    private OAuthClientFactory oAuthClientFactory;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private OAuthService oAuthService;

    @Nested
    @DisplayName("loginOrRegister")
    class LoginOrRegister {

        @Test
        @DisplayName("기존 소셜 계정으로 로그인한다")
        void loginWithExistingSocialAccount() {
            // given
            String code = "auth-code";
            OAuthProvider provider = OAuthProvider.GOOGLE;

            OAuthUserInfo userInfo = OAuthUserInfo.builder()
                    .provider(OAuthProvider.GOOGLE)
                    .providerId("google-123")
                    .email("user@gmail.com")
                    .name("홍길동")
                    .build();

            OAuthClient mockClient = mock(OAuthClient.class);
            when(oAuthClientFactory.getClient(provider)).thenReturn(mockClient);
            when(mockClient.getUserInfo(code)).thenReturn(userInfo);

            SocialAccount socialAccount = mock(SocialAccount.class);
            when(socialAccount.getMemberId()).thenReturn(1L);

            Member member = mock(Member.class);
            when(member.getId()).thenReturn(1L);
            when(member.getEmail()).thenReturn("user@gmail.com");
            when(member.getRole()).thenReturn(Role.USER);

            when(socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.of(socialAccount));
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(tokenProvider.createAccessToken(any(), any(), any())).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            OAuthLoginResult result = oAuthService.loginOrRegister(provider, code);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            verify(socialAccountRepository, never()).save(any());
        }

        @Test
        @DisplayName("신규 소셜 사용자를 등록한다 - 랜덤 닉네임 자동 생성")
        void registerNewSocialUser() {
            // given
            String code = "auth-code";
            OAuthProvider provider = OAuthProvider.GOOGLE;

            OAuthUserInfo userInfo = OAuthUserInfo.builder()
                    .provider(OAuthProvider.GOOGLE)
                    .providerId("google-123")
                    .email("newuser@gmail.com")
                    .name("신규유저")
                    .build();

            OAuthClient mockClient = mock(OAuthClient.class);
            when(oAuthClientFactory.getClient(provider)).thenReturn(mockClient);
            when(mockClient.getUserInfo(code)).thenReturn(userInfo);

            Member savedMember = mock(Member.class);
            when(savedMember.getId()).thenReturn(1L);
            when(savedMember.getEmail()).thenReturn("newuser@gmail.com");
            when(savedMember.getRole()).thenReturn(Role.USER);
            when(savedMember.getNickname()).thenReturn("사용자_a1b2c3d4");

            when(socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "google-123"))
                    .thenReturn(Optional.empty());
            when(memberRepository.findByEmail("newuser@gmail.com"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
            when(tokenProvider.createAccessToken(any(), any(), any())).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            OAuthLoginResult result = oAuthService.loginOrRegister(provider, code);

            // then
            assertThat(result.getMember().getNickname()).isNotNull();
            verify(memberRepository).save(any(Member.class));
            verify(socialAccountRepository).save(any(SocialAccount.class));
        }

        @Test
        @DisplayName("이미 가입된 이메일에 소셜 계정을 연동한다")
        void linkToExistingEmail() {
            // given
            String code = "auth-code";
            OAuthProvider provider = OAuthProvider.GITHUB;

            OAuthUserInfo userInfo = OAuthUserInfo.builder()
                    .provider(OAuthProvider.GITHUB)
                    .providerId("github-456")
                    .email("existing@example.com")
                    .name("기존유저")
                    .build();

            OAuthClient mockClient = mock(OAuthClient.class);
            when(oAuthClientFactory.getClient(provider)).thenReturn(mockClient);
            when(mockClient.getUserInfo(code)).thenReturn(userInfo);

            Member existingMember = mock(Member.class);
            when(existingMember.getId()).thenReturn(1L);
            when(existingMember.getEmail()).thenReturn("existing@example.com");
            when(existingMember.getRole()).thenReturn(Role.USER);

            when(socialAccountRepository.findByProviderAndProviderId(OAuthProvider.GITHUB, "github-456"))
                    .thenReturn(Optional.empty());
            when(memberRepository.findByEmail("existing@example.com"))
                    .thenReturn(Optional.of(existingMember));
            when(tokenProvider.createAccessToken(any(), any(), any())).thenReturn("access-token");
            when(tokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
            when(tokenProvider.getRefreshTokenValidity()).thenReturn(86400L);

            // when
            OAuthLoginResult result = oAuthService.loginOrRegister(provider, code);

            // then
            verify(socialAccountRepository).save(any(SocialAccount.class));
            verify(memberRepository, never()).save(any(Member.class));
        }
    }
}
