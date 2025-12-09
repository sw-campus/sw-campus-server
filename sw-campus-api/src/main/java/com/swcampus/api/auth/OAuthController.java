package com.swcampus.api.auth;

import com.swcampus.api.auth.request.OAuthCallbackRequest;
import com.swcampus.api.auth.response.OAuthLoginResponse;
import com.swcampus.api.config.CookieUtil;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.oauth.OAuthLoginResult;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

    @PostMapping("/{provider}")
    public ResponseEntity<OAuthLoginResponse> oauthLogin(
            @PathVariable("provider") String provider,
            @Valid @RequestBody OAuthCallbackRequest request) {

        OAuthProvider oAuthProvider = OAuthProvider.valueOf(provider.toUpperCase());
        OAuthLoginResult result = oAuthService.loginOrRegister(oAuthProvider, request.getCode());

        ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(
                result.getAccessToken(), tokenProvider.getAccessTokenValidity());
        ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
                result.getRefreshToken(), tokenProvider.getRefreshTokenValidity());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(OAuthLoginResponse.from(result));
    }
}
