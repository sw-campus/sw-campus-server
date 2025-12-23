package com.swcampus.api.auth.request;

import com.swcampus.domain.auth.SignupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "일반 회원가입 요청")
public class SignupRequest {

    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Schema(description = "닉네임 (영문, 숫자, 한글, -, _ 만 허용, 최대 20자)", example = "길동이", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 20, message = "닉네임은 20자 이내여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣_-]+$", message = "닉네임은 영문, 숫자, 한글, -, _ 만 사용할 수 있습니다")
    private String nickname;

    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phone;

    @Schema(description = "주소", example = "서울시 강남구", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String location;

    public SignupCommand toCommand() {
        return SignupCommand.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname)
                .phone(phone)
                .location(location)
                .build();
    }
}
