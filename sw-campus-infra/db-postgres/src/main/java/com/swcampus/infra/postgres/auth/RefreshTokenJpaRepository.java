package com.swcampus.infra.postgres.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByMemberId(Long memberId);
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByMemberId(Long memberId);
}
