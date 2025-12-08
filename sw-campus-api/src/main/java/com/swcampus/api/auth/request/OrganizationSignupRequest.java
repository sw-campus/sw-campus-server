package com.swcampus.api.auth.request;

import com.swcampus.domain.auth.OrganizationSignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class OrganizationSignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @NotBlank(message = "주소는 필수입니다")
    private String location;

    @NotBlank(message = "기관명은 필수입니다")
    private String organizationName;

    public OrganizationSignupCommand toCommand(MultipartFile certificateImage) throws IOException {
        return OrganizationSignupCommand.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname)
                .phone(phone)
                .location(location)
                .organizationName(organizationName)
                .certificateImage(certificateImage != null ? certificateImage.getBytes() : null)
                .certificateFileName(certificateImage != null ? certificateImage.getOriginalFilename() : null)
                .certificateContentType(certificateImage != null ? certificateImage.getContentType() : null)
                .build();
    }
}
