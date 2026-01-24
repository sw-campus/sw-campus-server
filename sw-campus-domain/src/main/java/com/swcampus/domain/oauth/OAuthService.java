package com.swcampus.domain.oauth;

import com.swcampus.domain.auth.RefreshToken;
import com.swcampus.domain.auth.RefreshTokenRepository;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final OAuthClientFactory oAuthClientFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * OAuth 로그인 또는 회원가입 처리
     * @param provider OAuth 제공자 (GOOGLE, GITHUB)
     * @param code Authorization code
     * @return 로그인 결과 (토큰, 회원 정보)
     */
    @Transactional
    public OAuthLoginResult loginOrRegister(OAuthProvider provider, String code) {
        OAuthClient client = oAuthClientFactory.getClient(provider);
        OAuthUserInfo userInfo = client.getUserInfo(code);

        return processOAuthLogin(userInfo);
    }

    /**
     * OAuth 로그인 처리 (내부 로직)
     */
    private OAuthLoginResult processOAuthLogin(OAuthUserInfo userInfo) {
        // 1. 소셜 계정으로 기존 회원 조회
        Optional<SocialAccount> existingSocialAccount = socialAccountRepository
                .findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId());

        Member member;
        boolean isFirstLogin = false;

        if (existingSocialAccount.isPresent()) {
            // 기존 소셜 계정이 있으면 해당 회원으로 로그인
            member = memberRepository.findById(existingSocialAccount.get().getMemberId())
                    .orElseThrow(() -> new MemberNotFoundException(existingSocialAccount.get().getMemberId()));
        } else {
            // 이메일로 기존 회원 조회
            Optional<Member> existingMember = memberRepository.findByEmail(userInfo.getEmail());

            if (existingMember.isPresent()) {
                // 기존 회원이 있으면 소셜 계정 연동
                member = existingMember.get();
                linkSocialAccount(member, userInfo);
            } else {
                // 신규 회원 생성 (랜덤 닉네임 자동 생성)
                member = createOAuthMember(userInfo);
                isFirstLogin = true;
            }
        }

        // 토큰 발급
        return issueTokens(member, isFirstLogin);
    }

    /**
     * 소셜 계정 연동
     */
    private void linkSocialAccount(Member member, OAuthUserInfo userInfo) {
        SocialAccount socialAccount = SocialAccount.create(
                member.getId(),
                userInfo.getProvider(),
                userInfo.getProviderId()
        );
        socialAccountRepository.save(socialAccount);
    }

    /**
     * OAuth 신규 회원 생성 (랜덤 닉네임 자동 생성)
     */
    private Member createOAuthMember(OAuthUserInfo userInfo) {
        Member member = Member.createOAuthUser(userInfo.getEmail(), userInfo.getName());
        Member savedMember = memberRepository.save(member);

        // 소셜 계정 연동
        SocialAccount socialAccount = SocialAccount.create(
                savedMember.getId(),
                userInfo.getProvider(),
                userInfo.getProviderId()
        );
        socialAccountRepository.save(socialAccount);

        return savedMember;
    }

    /**
     * 토큰 발급
     */
    private OAuthLoginResult issueTokens(Member member, boolean isFirstLogin) {
        // 기존 RT 삭제
        refreshTokenRepository.deleteByMemberId(member.getId());

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(
                member.getId(), member.getEmail(), member.getRole());
        String refreshToken = tokenProvider.createRefreshToken(member.getId());

        // RT 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(
                member.getId(),
                refreshToken,
                tokenProvider.getRefreshTokenValidity()
        );
        refreshTokenRepository.save(refreshTokenEntity);

        return new OAuthLoginResult(accessToken, refreshToken, member, isFirstLogin);
    }
}
