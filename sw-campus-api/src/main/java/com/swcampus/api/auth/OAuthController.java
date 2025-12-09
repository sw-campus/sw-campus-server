package com.swcampus.api.auth;

import com.swcampus.api.auth.request.OAuthCallbackRequest;
import com.swcampus.api.auth.response.OAuthLoginResponse;
import com.swcampus.api.config.CookieUtil;
import com.swcampus.api.exception.ErrorResponse;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.oauth.OAuthLoginResult;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth", description = "소셜 로그인 API")
public class OAuthController {

    private final OAuthService oAuthService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

    @PostMapping("/{provider}")
    @Operation(
        summary = "소셜 로그인",
        description = "Google/GitHub Authorization Code로 로그인합니다. 신규 사용자는 자동으로 회원가입됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 코드, 지원하지 않는 provider)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 코드)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OAuthLoginResponse> oauthLogin(
            @Parameter(description = "OAuth Provider", example = "google", required = true,
                schema = @Schema(allowableValues = {"google", "github"}))
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
