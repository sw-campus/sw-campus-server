package com.swcampus.api.organization.response;

import com.swcampus.domain.organization.Organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "기관 상세 정보 응답")
public class OrganizationResponse {

    @Schema(description = "기관 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "10")
    private Long userId;

    @Schema(description = "기관명", example = "패스트캠퍼스")
    private String name;

    @Schema(description = "기관 설명", example = "IT 전문 교육 기관입니다.")
    private String description;

    @Schema(description = "사업자등록증 S3 Key", example = "employment-certificates/2024/01/01/uuid.jpg")
    private String certificateKey;

    @Schema(description = "정부 인증 여부", example = "K-DIGITAL")
    private String govAuth;

    @Schema(description = "시설 이미지 URL 1", example = "https://example.com/facility1.jpg")
    private String facilityImageUrl;

    @Schema(description = "시설 이미지 URL 2", example = "https://example.com/facility2.jpg")
    private String facilityImageUrl2;

    @Schema(description = "시설 이미지 URL 3", example = "https://example.com/facility3.jpg")
    private String facilityImageUrl3;

    @Schema(description = "시설 이미지 URL 4", example = "https://example.com/facility4.jpg")
    private String facilityImageUrl4;

    @Schema(description = "기관 로고 URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "기관 홈페이지 URL", example = "https://example.com")
    private String homepage;

    public static OrganizationResponse from(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .userId(organization.getUserId())
                .name(organization.getName())
                .description(organization.getDescription())
                .certificateKey(organization.getCertificateKey())
                .govAuth(organization.getGovAuth())
                .facilityImageUrl(organization.getFacilityImageUrl())
                .facilityImageUrl2(organization.getFacilityImageUrl2())
                .facilityImageUrl3(organization.getFacilityImageUrl3())
                .facilityImageUrl4(organization.getFacilityImageUrl4())
                .logoUrl(organization.getLogoUrl())
                .homepage(organization.getHomepage())
                .build();
    }
}
