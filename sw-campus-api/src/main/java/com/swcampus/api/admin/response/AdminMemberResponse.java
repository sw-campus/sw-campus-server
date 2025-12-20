package com.swcampus.api.admin.response;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.Role;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 회원 조회 응답")
public record AdminMemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "닉네임", example = "길동이")
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "역할", example = "USER")
        Role role
) {
    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole()
        );
    }
}
