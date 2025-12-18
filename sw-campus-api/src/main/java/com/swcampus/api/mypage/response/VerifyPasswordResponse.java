package com.swcampus.api.mypage.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 확인 응답")
public record VerifyPasswordResponse(
    @Schema(description = "일치 여부", example = "true")
    boolean valid
) {
    public static VerifyPasswordResponse from(boolean valid) {
        return new VerifyPasswordResponse(valid);
    }
}
