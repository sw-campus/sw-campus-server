package com.swcampus.api.organization.response;

import com.swcampus.domain.organization.Organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "기관 요약 정보 응답")
public record OrganizationSummaryResponse(
    @Schema(description = "기관 ID", example = "1")
    Long id,

    @Schema(description = "기관명", example = "패스트캠퍼스")
    String name,

    @Schema(description = "기관 로고 URL", example = "https://example.com/logo.png")
    String logoUrl,

    @Schema(description = "기관 설명", example = "IT 전문 교육 기관입니다.")
    String description,

    @Schema(description = "모집 중인 강의 수", example = "5")
    Long recruitingLectureCount
) {
    public static OrganizationSummaryResponse from(Organization organization, Long recruitingLectureCount) {
        return OrganizationSummaryResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .logoUrl(organization.getLogoUrl())
                .description(organization.getDescription())
                .recruitingLectureCount(recruitingLectureCount)
                .build();
    }
}
