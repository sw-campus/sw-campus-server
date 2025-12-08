package com.swcampus.api.auth;

import com.swcampus.api.auth.request.EmailSendRequest;
import com.swcampus.api.auth.request.LoginRequest;
import com.swcampus.api.auth.request.OrganizationSignupRequest;
import com.swcampus.api.auth.request.SignupRequest;
import com.swcampus.api.auth.response.EmailStatusResponse;
import com.swcampus.api.auth.response.LoginResponse;
import com.swcampus.api.auth.response.MessageResponse;
import com.swcampus.api.auth.response.OrganizationSignupResponse;
import com.swcampus.api.auth.response.SignupResponse;
import com.swcampus.api.config.CookieUtil;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.auth.LoginResult;
import com.swcampus.domain.auth.OrganizationSignupResult;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.member.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/email/send")
    public ResponseEntity<MessageResponse> sendVerificationEmail(
            @Valid @RequestBody EmailSendRequest request) {
        emailService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("인증 메일이 발송되었습니다"));
    }

    @GetMapping("/email/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        try {
            emailService.verifyEmail(token);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/signup?verified=true"))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/signup?error=invalid_token"))
                    .build();
        }
    }

    @GetMapping("/email/status")
    public ResponseEntity<EmailStatusResponse> checkEmailStatus(
            @RequestParam("email") String email) {
        boolean verified = emailService.isEmailVerified(email);
        return ResponseEntity.ok(EmailStatusResponse.of(email, verified));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        Member member = authService.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SignupResponse.from(member));
    }

    @PostMapping(value = "/signup/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrganizationSignupResponse> signupOrganization(
            @Valid @ModelAttribute OrganizationSignupRequest request,
            @RequestParam("certificateImage") MultipartFile certificateImage) throws IOException {

        OrganizationSignupResult result = authService.signupOrganization(request.toCommand(certificateImage));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrganizationSignupResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.getEmail(), request.getPassword());

        ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(
                result.getAccessToken(), tokenProvider.getAccessTokenValidity());
        ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
                result.getRefreshToken(), tokenProvider.getRefreshTokenValidity());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(LoginResponse.from(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "accessToken", required = false) String accessToken) {

        if (accessToken != null && tokenProvider.validateToken(accessToken)) {
            Long memberId = tokenProvider.getMemberId(accessToken);
            authService.logout(memberId);
        }

        ResponseCookie deleteAccessCookie = cookieUtil.deleteAccessTokenCookie();
        ResponseCookie deleteRefreshCookie = cookieUtil.deleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new InvalidTokenException();
        }

        String newAccessToken = authService.refresh(refreshToken);

        ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(
                newAccessToken, tokenProvider.getAccessTokenValidity());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
    }
}
