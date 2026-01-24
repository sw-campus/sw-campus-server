package com.swcampus.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swcampus.domain.auth.LoginResult;
import com.swcampus.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "회원가입 응답")
public class SignupResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "닉네임", example = "길동이")
    private String nickname;

    @Schema(description = "권한", example = "USER")
    private String role;

    @JsonProperty("isFirstLogin")
    @Schema(description = "최초 로그인 여부", example = "true")
    private boolean isFirstLogin;

    public static SignupResponse from(LoginResult result) {
        Member member = result.getMember();
        return new SignupResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getRole().name(),
                result.isFirstLogin()
        );
    }
}
