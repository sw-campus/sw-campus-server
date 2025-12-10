package com.swcampus.domain.oauth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {
    private Long id;
    private Long memberId;
    private OAuthProvider provider;
    private String providerId;
    private LocalDateTime createdAt;

    public static SocialAccount create(Long memberId, OAuthProvider provider, String providerId) {
        SocialAccount account = new SocialAccount();
        account.memberId = memberId;
        account.provider = provider;
        account.providerId = providerId;
        account.createdAt = LocalDateTime.now();
        return account;
    }

    public static SocialAccount of(Long id, Long memberId, OAuthProvider provider,
                                   String providerId, LocalDateTime createdAt) {
        SocialAccount account = new SocialAccount();
        account.id = id;
        account.memberId = memberId;
        account.provider = provider;
        account.providerId = providerId;
        account.createdAt = createdAt;
        return account;
    }
}
