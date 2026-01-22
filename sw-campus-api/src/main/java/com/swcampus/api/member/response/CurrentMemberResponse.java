package com.swcampus.api.member.response;

import com.swcampus.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "현재 사용자 정보 응답")
public class CurrentMemberResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 역할", example = "USER")
    private String role;

    public static CurrentMemberResponse from(Member member) {
        return CurrentMemberResponse.builder()
                .userId(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();
    }
}
