package com.swcampus.api.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthCallbackRequest {

    @NotBlank(message = "인증 코드는 필수입니다")
    private String code;
}
