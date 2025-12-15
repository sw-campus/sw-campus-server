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

    @Schema(description = "대표자명")
    String representativeName,

    @Schema(description = "전화번호")
    String phone,

    @Schema(description = "주소")
    String location,

    @Schema(description = "승인 상태")
    ApprovalStatus approvalStatus
) {
    public static OrganizationInfoResponse from(Organization organization, Member member) {
        return new OrganizationInfoResponse(
            organization.getId(),
            organization.getName(),
            member.getName(), // 대표자명은 Member 이름으로 가정
            member.getPhone(),
            member.getLocation(),
            organization.getApprovalStatus()
        );
    }
}
