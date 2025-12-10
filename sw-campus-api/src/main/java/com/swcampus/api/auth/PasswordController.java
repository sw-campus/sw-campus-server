package com.swcampus.api.auth;

import com.swcampus.api.auth.request.PasswordChangeRequest;
import com.swcampus.api.auth.request.TemporaryPasswordRequest;
import com.swcampus.domain.auth.PasswordService;
import com.swcampus.domain.auth.TokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;
    private final TokenProvider tokenProvider;

    @PatchMapping
    public ResponseEntity<Void> changePassword(
            @CookieValue(name = "accessToken") String accessToken,
            @Valid @RequestBody PasswordChangeRequest request) {

        Long userId = tokenProvider.getMemberId(accessToken);
        passwordService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/temporary")
    public ResponseEntity<Map<String, String>> issueTemporaryPassword(
            @Valid @RequestBody TemporaryPasswordRequest request) {

        passwordService.issueTemporaryPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 발송되었습니다"));
    }
}
