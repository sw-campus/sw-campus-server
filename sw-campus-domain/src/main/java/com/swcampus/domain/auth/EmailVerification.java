package com.swcampus.domain.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {
    private Long id;
    private String email;
    private String token;
    private boolean verified;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private static final int EXPIRATION_HOURS = 24;

    public static EmailVerification create(String email) {
        EmailVerification ev = new EmailVerification();
        ev.email = email;
        ev.token = UUID.randomUUID().toString();
        ev.verified = false;
        ev.expiresAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
        ev.createdAt = LocalDateTime.now();
        return ev;
    }

    public static EmailVerification of(Long id, String email, String token,
                                       boolean verified, LocalDateTime expiresAt,
                                       LocalDateTime createdAt) {
        EmailVerification ev = new EmailVerification();
        ev.id = id;
        ev.email = email;
        ev.token = token;
        ev.verified = verified;
        ev.expiresAt = expiresAt;
        ev.createdAt = createdAt;
        return ev;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void verify() {
        if (isExpired()) {
            throw new IllegalStateException("인증 토큰이 만료되었습니다");
        }
        this.verified = true;
    }
}
