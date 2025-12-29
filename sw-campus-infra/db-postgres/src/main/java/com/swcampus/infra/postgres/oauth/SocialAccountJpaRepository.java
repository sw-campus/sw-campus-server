package com.swcampus.infra.postgres.oauth;

import com.swcampus.domain.oauth.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountJpaRepository extends JpaRepository<SocialAccountEntity, Long> {
    Optional<SocialAccountEntity> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    List<SocialAccountEntity> findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);
}
