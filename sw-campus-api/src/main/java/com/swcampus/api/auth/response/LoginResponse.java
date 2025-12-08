package com.swcampus.api.auth.response;

import com.swcampus.domain.auth.LoginResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String role;

    // ORGANIZATION인 경우만 포함
    private Long organizationId;
    private String organizationName;
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
