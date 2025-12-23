package com.swcampus.api.organization.response;

import java.time.LocalDateTime;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.organization.Organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrganizationSummaryResponse {
    @Schema(description = "기관 ID", example = "1")
    private Long id;

    @Schema(description = "기관명", example = "테스트교육기관")
    private String name;

    @Schema(description = "승인 상태", example = "PENDING")
    private ApprovalStatus approvalStatus;

    @Schema(description = "신청일시", example = "2025-12-15T10:00:00")
    private LocalDateTime createdAt;

    public static AdminOrganizationSummaryResponse from(Organization organization) {
        return AdminOrganizationSummaryResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .approvalStatus(organization.getApprovalStatus())
                .createdAt(organization.getCreatedAt())
                .build();
    }
}
