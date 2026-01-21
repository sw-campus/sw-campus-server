package com.swcampus.api.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청")
public class PasswordChangeRequest {

    @Schema(description = "현재 비밀번호", example = "CurrentPass123!")
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "NewPass456!")
    @NotBlank(message = "새 비밀번호는 필수입니다")
    private String newPassword;
}
