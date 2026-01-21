package com.swcampus.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("RefreshToken 생성")
    void create() {
        // given
        Long memberId = 1L;
        String token = "refresh-token-value";
        long expirationSeconds = 604800; // 7일

        // when
        RefreshToken rt = RefreshToken.create(memberId, token, expirationSeconds);

        // then
        assertThat(rt.getMemberId()).isEqualTo(memberId);
        assertThat(rt.getToken()).isEqualTo(token);
        assertThat(rt.getExpiresAt()).isNotNull();
        assertThat(rt.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("토큰 갱신")
    void updateToken() {
        // given
        RefreshToken rt = RefreshToken.create(1L, "old-token", 604800);
        String newToken = "new-token";

        // when
        rt.updateToken(newToken, 604800);

        // then
        assertThat(rt.getToken()).isEqualTo(newToken);
    }

    @Test
    @DisplayName("만료되지 않은 토큰은 isExpired가 false")
    void isExpired_notExpired() {
        // given
        RefreshToken rt = RefreshToken.create(1L, "token", 604800);

        // when & then
        assertThat(rt.isExpired()).isFalse();
    }
}
