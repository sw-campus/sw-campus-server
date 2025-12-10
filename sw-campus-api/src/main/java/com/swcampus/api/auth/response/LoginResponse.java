package com.swcampus.api.auth.response;

import com.swcampus.domain.auth.LoginResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "로그인 응답")
public class LoginResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "닉네임", example = "길동이")
    private String nickname;

    @Schema(description = "권한", example = "USER", allowableValues = {"USER", "ADMIN", "PROVIDER"})
    private String role;

    @Schema(description = "기관 ID (기관 회원만)", example = "1", nullable = true)
    private Long organizationId;

    @Schema(description = "기관명 (기관 회원만)", example = "ABC교육원", nullable = true)
    private String organizationName;

    @Schema(description = "승인 상태 (기관 회원만)", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"}, nullable = true)
    private String approvalStatus;

    public static LoginResponse from(LoginResult result) {
        LoginResponseBuilder builder = LoginResponse.builder()
                .userId(result.getMember().getId())
                .email(result.getMember().getEmail())
                .name(result.getMember().getName())
                .nickname(result.getMember().getNickname())
                .role(result.getMember().getRole().name());

        if (result.getOrganization() != null) {
            builder.organizationId(result.getOrganization().getId())
                    .organizationName(result.getOrganization().getName())
                    .approvalStatus(result.getOrganization().getApprovalStatus().name());
        }

        return builder.build();
    }
}
