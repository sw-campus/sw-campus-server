package com.swcampus.api.organization.response;

import java.time.LocalDateTime;

import com.swcampus.domain.organization.ApprovalStatus;
import com.swcampus.domain.organization.Organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrganizationDetailResponse {
    @Schema(description = "기관 ID", example = "1")
    private Long id;

    @Schema(description = "기관명", example = "테스트교육기관")
    private String name;

    @Schema(description = "기관 설명", example = "기관 설명입니다.")
    private String description;

    @Schema(description = "재직증명서 URL", example = "https://s3.../certificate.jpg")
    private String certificateUrl;

    @Schema(description = "승인 상태", example = "PENDING")
    private ApprovalStatus approvalStatus;

    @Schema(description = "홈페이지 URL", example = "https://example.com")
    private String homepage;

    @Schema(description = "신청일시", example = "2025-12-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-12-15T10:00:00")
    private LocalDateTime updatedAt;

    public static AdminOrganizationDetailResponse from(Organization organization) {
        return AdminOrganizationDetailResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .certificateUrl(organization.getCertificateUrl())
                .approvalStatus(organization.getApprovalStatus())
                .homepage(organization.getHomepage())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }
}
