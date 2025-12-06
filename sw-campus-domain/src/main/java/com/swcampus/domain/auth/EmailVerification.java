package com.swcampus.domain.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {
    private Long id;
    private String email;
    private String code;
    private boolean verified;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private static final int EXPIRATION_MINUTES = 5;

    public static EmailVerification create(String email, String code) {
        EmailVerification ev = new EmailVerification();
        ev.email = email;
        ev.code = code;
        ev.verified = false;
        ev.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        ev.createdAt = LocalDateTime.now();
        return ev;
    }

    public static EmailVerification of(Long id, String email, String code,
                                       boolean verified, LocalDateTime expiresAt,
                                       LocalDateTime createdAt) {
        EmailVerification ev = new EmailVerification();
        ev.id = id;
        ev.email = email;
        ev.code = code;
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
            throw new IllegalStateException("인증 코드가 만료되었습니다");
        }
        this.verified = true;
    }

    public boolean matchCode(String inputCode) {
        return this.code.equals(inputCode);
    }
}
