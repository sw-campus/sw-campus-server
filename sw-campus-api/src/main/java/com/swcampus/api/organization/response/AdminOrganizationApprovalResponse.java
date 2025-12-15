package com.swcampus.api.organization.response;

import com.swcampus.domain.organization.ApprovalStatus;
import com.swcampus.domain.organization.Organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrganizationApprovalResponse {
    @Schema(description = "기관 ID", example = "1")
    private Long id;

    @Schema(description = "승인 상태", example = "APPROVED")
    private ApprovalStatus approvalStatus;

    @Schema(description = "메시지", example = "기관이 승인되었습니다.")
    private String message;

    public static AdminOrganizationApprovalResponse of(Organization organization, String message) {
        return AdminOrganizationApprovalResponse.builder()
                .id(organization.getId())
                .approvalStatus(organization.getApprovalStatus())
                .message(message)
                .build();
    }
}
