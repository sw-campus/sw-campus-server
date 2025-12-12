package com.swcampus.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 발송 요청")
public class EmailSendRequest {

    @Schema(description = "인증할 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "가입 유형 (personal: 일반, organization: 기관)", example = "personal", defaultValue = "personal")
    private String signupType = "personal";

    public EmailSendRequest(String email) {
        this.email = email;
        this.signupType = "personal";
    }
}
