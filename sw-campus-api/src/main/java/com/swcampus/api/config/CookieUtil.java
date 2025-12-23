package com.swcampus.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final String ACCESS_TOKEN_NAME = "accessToken";
    private static final String REFRESH_TOKEN_NAME = "refreshToken";
    private static final String VERIFIED_EMAIL_NAME = "verifiedEmail";
    private static final long VERIFIED_EMAIL_MAX_AGE = 300; // 5분

    @Value("${app.cookie.secure:true}")
    private boolean secure;

    public ResponseCookie createAccessTokenCookie(String token, long maxAge) {
        return ResponseCookie.from(ACCESS_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token, long maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie createVerifiedEmailCookie(String email) {
        return ResponseCookie.from(VERIFIED_EMAIL_NAME, email)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(VERIFIED_EMAIL_MAX_AGE)
                .build();
    }

    public ResponseCookie deleteVerifiedEmailCookie() {
        return ResponseCookie.from(VERIFIED_EMAIL_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }
}
