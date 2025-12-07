package com.swcampus.infra.postgres.auth;

import com.swcampus.domain.auth.RefreshToken;
import com.swcampus.domain.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = RefreshTokenEntity.from(refreshToken);
        RefreshTokenEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId)
                .map(RefreshTokenEntity::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(RefreshTokenEntity::toDomain);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        jpaRepository.deleteByMemberId(memberId);
    }
}
