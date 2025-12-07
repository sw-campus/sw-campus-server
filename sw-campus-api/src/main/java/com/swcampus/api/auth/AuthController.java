package com.swcampus.api.auth;

import com.swcampus.api.auth.request.EmailSendRequest;
import com.swcampus.api.auth.request.SignupRequest;
import com.swcampus.api.auth.response.EmailStatusResponse;
import com.swcampus.api.auth.response.MessageResponse;
import com.swcampus.api.auth.response.SignupResponse;
import com.swcampus.domain.auth.AuthService;
import com.swcampus.domain.auth.EmailService;
import com.swcampus.domain.member.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

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
}
