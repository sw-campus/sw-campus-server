package com.swcampus.api.member.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 요청")
public record WithdrawRequest(
    @Schema(description = "비밀번호 (OAuth 사용자는 빈 값 가능)", example = "password123")
    String password
) {
}
