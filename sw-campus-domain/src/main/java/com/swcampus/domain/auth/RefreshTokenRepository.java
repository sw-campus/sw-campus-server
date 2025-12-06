package com.swcampus.domain.auth;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByMemberId(Long memberId);
    Optional<RefreshToken> findByToken(String token);
    void deleteByMemberId(Long memberId);
}
