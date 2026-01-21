package com.swcampus.domain.oauth;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository {
    SocialAccount save(SocialAccount socialAccount);
    Optional<SocialAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    List<SocialAccount> findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);
}
