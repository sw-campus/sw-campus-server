package com.swcampus.infra.postgres.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByMemberId(Long memberId);
    Optional<RefreshTokenEntity> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
