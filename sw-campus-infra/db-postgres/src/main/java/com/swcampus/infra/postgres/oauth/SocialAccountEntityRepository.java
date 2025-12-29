package com.swcampus.infra.postgres.oauth;

import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.SocialAccount;
import com.swcampus.domain.oauth.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SocialAccountEntityRepository implements SocialAccountRepository {

    private final SocialAccountJpaRepository jpaRepository;

    @Override
    public SocialAccount save(SocialAccount socialAccount) {
        SocialAccountEntity entity = SocialAccountEntity.from(socialAccount);
        SocialAccountEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<SocialAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId)
                .map(SocialAccountEntity::toDomain);
    }

    @Override
    public List<SocialAccount> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).stream()
                .map(SocialAccountEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        jpaRepository.deleteByMemberId(memberId);
    }
}
