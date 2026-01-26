package com.swcampus.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.oauth.OAuthLoginResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "OAuth 로그인 응답")
public class OAuthLoginResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "이메일", example = "user@gmail.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "닉네임 (자동 생성)", example = "사용자_a1b2c3d4")
    private String nickname;

    @Schema(description = "권한", example = "USER")
    private String role;

    @JsonProperty("isFirstLogin")
    @Schema(description = "최초 로그인 여부", example = "false")
    private boolean isFirstLogin;

    public static OAuthLoginResponse from(OAuthLoginResult result) {
        Member member = result.getMember();
        return OAuthLoginResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .role(member.getRole().name())
                .isFirstLogin(result.isFirstLogin())
                .build();
    }
}
