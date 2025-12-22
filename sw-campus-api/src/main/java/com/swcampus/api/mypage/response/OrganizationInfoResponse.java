package com.swcampus.api.mypage.response;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.organization.ApprovalStatus;
import com.swcampus.domain.organization.Organization;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기관 정보 응답")
public record OrganizationInfoResponse(
    @Schema(description = "기관 ID")
    Long organizationId,

    @Schema(description = "기관명")
    String organizationName,

    @Schema(description = "기관 설명")
    String description,

    @Schema(description = "대표자명")
    String representativeName,

    @Schema(description = "전화번호")
    String phone,

    @Schema(description = "주소")
    String location,

    @Schema(description = "승인 상태")
    ApprovalStatus approvalStatus,

    @Schema(description = "사업자등록증 S3 Key")
    String certificateKey,

    @Schema(description = "정부 인증 정보")
    String govAuth,

    @Schema(description = "시설 이미지 URL 1")
    String facilityImageUrl,

    @Schema(description = "시설 이미지 URL 2")
    String facilityImageUrl2,

    @Schema(description = "시설 이미지 URL 3")
    String facilityImageUrl3,

    @Schema(description = "시설 이미지 URL 4")
    String facilityImageUrl4,

    @Schema(description = "기관 로고 URL")
    String logoUrl,

    @Schema(description = "홈페이지 URL")
    String homepage
) {
    public static OrganizationInfoResponse from(Organization organization, Member member) {
        return new OrganizationInfoResponse(
            organization.getId(),
            organization.getName(),
            organization.getDescription(),
            member.getName(), // 대표자명은 Member 이름으로 가정
            member.getPhone(),
            member.getLocation(),
            organization.getApprovalStatus(),
            organization.getCertificateKey(),
            organization.getGovAuth(),
            organization.getFacilityImageUrl(),
            organization.getFacilityImageUrl2(),
            organization.getFacilityImageUrl3(),
            organization.getFacilityImageUrl4(),
            organization.getLogoUrl(),
            organization.getHomepage()
        );
    }
}
