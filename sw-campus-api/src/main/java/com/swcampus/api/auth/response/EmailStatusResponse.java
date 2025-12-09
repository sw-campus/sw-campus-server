package com.swcampus.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "이메일 인증 상태 응답")
public class EmailStatusResponse {

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "인증 완료 여부", example = "true")
    private boolean verified;

    public static EmailStatusResponse of(String email, boolean verified) {
        return new EmailStatusResponse(email, verified);
    }
}
