package com.swcampus.api.auth.request;

import com.swcampus.domain.auth.OrganizationSignupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "기관 회원가입 요청")
public class OrganizationSignupRequest {

    @Schema(description = "이메일", example = "provider@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @Schema(description = "이름", example = "김대표", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Schema(description = "닉네임", example = "대표님", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    @Schema(description = "전화번호", example = "010-9876-5432", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @Schema(description = "주소", example = "서울시 서초구", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "주소는 필수입니다")
    private String location;

    @Schema(description = "기관명", example = "ABC교육원", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "기관명은 필수입니다")
    private String organizationName;

    @Schema(description = "재직증명서 이미지", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "재직증명서는 필수입니다")
    private MultipartFile certificateImage;

    @Schema(description = "기존 기관 ID (선택사항. 기존 기관 선택 시 입력, 신규 기관은 입력하지 않음)", example = "1")
    private Long organizationId;

    public OrganizationSignupCommand toCommand() throws IOException {
        var builder = OrganizationSignupCommand.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname)
                .phone(phone)
                .location(location)
                .organizationName(organizationName)
                .organizationId(organizationId);

        if (certificateImage != null && !certificateImage.isEmpty()) {
            builder.certificateImage(certificateImage.getBytes())
                    .certificateFileName(certificateImage.getOriginalFilename())
                    .certificateContentType(certificateImage.getContentType());
        }

        return builder.build();
    }
}
