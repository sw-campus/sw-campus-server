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
import com.swcampus.api.exception.ErrorResponse;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.auth.LoginResult;
import com.swcampus.domain.auth.OrganizationSignupResult;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.auth.exception.InvalidTokenException;
import com.swcampus.domain.member.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증/인가 API")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/email/send")
    @Operation(summary = "인증 메일 발송", description = "회원가입을 위한 이메일 인증 메일을 발송합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "발송 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 가입된 이메일",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> sendVerificationEmail(
            @Valid @RequestBody EmailSendRequest request) {
        emailService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of("인증 메일이 발송되었습니다"));
    }

    @GetMapping("/email/verify")
    @Operation(summary = "이메일 인증 확인", description = "이메일 인증 링크 클릭 시 호출됩니다. 인증 완료 후 프론트엔드로 리다이렉트됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "인증 성공 - 프론트엔드로 리다이렉트"),
        @ApiResponse(responseCode = "302", description = "인증 실패 - 에러 페이지로 리다이렉트")
    })
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "인증 토큰", required = true)
            @RequestParam("token") String token) {
        try {
            emailService.verifyEmail(token);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/signup/personal?verified=true"))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/signup/personal?error=invalid_token"))
                    .build();
        }
    }

    @GetMapping("/email/status")
    @Operation(summary = "이메일 인증 상태 확인", description = "해당 이메일의 인증 완료 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<EmailStatusResponse> checkEmailStatus(
            @Parameter(description = "확인할 이메일", example = "user@example.com", required = true)
            @RequestParam("email") String email) {
        boolean verified = emailService.isEmailVerified(email);
        return ResponseEntity.ok(EmailStatusResponse.of(email, verified));
    }

    @PostMapping("/signup")
    @Operation(summary = "일반 회원가입", description = "이메일 인증 완료 후 일반 사용자로 회원가입합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이메일 중복",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        Member member = authService.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SignupResponse.from(member));
    }

    @PostMapping(value = "/signup/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "기관 회원가입", description = "기관 사용자로 회원가입합니다. 재직증명서 이미지가 필요하며, 관리자 승인 후 이용 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공 (승인 대기)"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrganizationSignupResponse> signupOrganization(
            @Valid @ModelAttribute OrganizationSignupRequest request,
            @Parameter(description = "재직증명서 이미지", required = true)
            @RequestParam("certificateImage") MultipartFile certificateImage) throws IOException {

        OrganizationSignupResult result = authService.signupOrganization(request.toCommand(certificateImage));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrganizationSignupResponse.from(result));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. 성공 시 JWT 토큰이 쿠키로 발급됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "로그아웃", description = "로그아웃 처리 후 쿠키를 삭제합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
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
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access Token을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
