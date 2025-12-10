package com.swcampus.api.organization.response;

import com.swcampus.domain.organization.Organization;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizationResponse {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String certificateUrl;
    private String govAuth;
    private String facilityImageUrl;
    private String facilityImageUrl2;
    private String facilityImageUrl3;
    private String facilityImageUrl4;
    private String logoUrl;

    public static OrganizationResponse from(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .userId(organization.getUserId())
                .name(organization.getName())
                .description(organization.getDescription())
                .certificateUrl(organization.getCertificateUrl())
                .govAuth(organization.getGovAuth())
                .facilityImageUrl(organization.getFacilityImageUrl())
                .facilityImageUrl2(organization.getFacilityImageUrl2())
                .facilityImageUrl3(organization.getFacilityImageUrl3())
                .facilityImageUrl4(organization.getFacilityImageUrl4())
                .logoUrl(organization.getLogoUrl())
                .build();
    }
}
