package com.swcampus.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 콜백 요청")
public class OAuthCallbackRequest {

    @Schema(description = "Authorization Code", example = "4/0AfJohXn...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "인증 코드는 필수입니다")
    private String code;
}
