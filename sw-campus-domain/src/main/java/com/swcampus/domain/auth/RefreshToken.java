package com.swcampus.domain.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    private Long id;
    private Long memberId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static RefreshToken create(Long memberId, String token, long expirationSeconds) {
        RefreshToken rt = new RefreshToken();
        rt.memberId = memberId;
        rt.token = token;
        rt.expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);
        rt.createdAt = LocalDateTime.now();
        return rt;
    }

    public static RefreshToken of(Long id, Long memberId, String token,
                                  LocalDateTime expiresAt, LocalDateTime createdAt) {
        RefreshToken rt = new RefreshToken();
        rt.id = id;
        rt.memberId = memberId;
        rt.token = token;
        rt.expiresAt = expiresAt;
        rt.createdAt = createdAt;
        return rt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void updateToken(String newToken, long expirationSeconds) {
        this.token = newToken;
        this.expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);
    }
}
