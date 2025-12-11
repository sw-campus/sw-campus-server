package com.swcampus.api.organization.response;

import com.swcampus.domain.organization.Organization;
import lombok.Builder;

@Builder
public record OrganizationSummaryResponse(
    Long id,
    String name,
    String logoUrl,
    String description,
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
