package com.swcampus.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "인증된 이메일 응답")
public class VerifiedEmailResponse {

    @Schema(description = "인증된 이메일", example = "user@example.com")
    private String email;

    public static VerifiedEmailResponse of(String email) {
        return new VerifiedEmailResponse(email);
    }
}
