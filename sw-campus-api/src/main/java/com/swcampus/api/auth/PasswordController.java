package com.swcampus.api.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.auth.request.PasswordChangeRequest;
import com.swcampus.api.auth.request.TemporaryPasswordRequest;
import com.swcampus.domain.auth.PasswordService;
import com.swcampus.domain.auth.TokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
@Tag(name = "Password", description = "비밀번호 관리 API")
public class PasswordController {

    private final PasswordService passwordService;
    private final TokenProvider tokenProvider;

    @PatchMapping
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공"),
        @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<Void> changePassword(
            @CookieValue(name = "accessToken") String accessToken,
            @Valid @RequestBody PasswordChangeRequest request) {

        Long userId = tokenProvider.getMemberId(accessToken);
        passwordService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/temporary")
    @Operation(summary = "임시 비밀번호 발급", description = "이름, 전화번호, 이메일이 모두 일치하는 경우 임시 비밀번호를 이메일로 발송합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "발송 성공"),
        @ApiResponse(responseCode = "404", description = "일치하는 사용자 없음")
    })
    public ResponseEntity<Map<String, String>> issueTemporaryPassword(
            @Valid @RequestBody TemporaryPasswordRequest request) {

        passwordService.issueTemporaryPassword(request.getEmail(), request.getName(), request.getPhone());
        return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 발송되었습니다"));
    }
}
