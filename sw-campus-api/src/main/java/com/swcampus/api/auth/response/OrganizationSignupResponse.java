package com.swcampus.api.auth.response;

import com.swcampus.domain.auth.OrganizationSignupResult;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.organization.Organization;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrganizationSignupResponse {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String role;
    private Long organizationId;
    private String organizationName;
    private String approvalStatus;
    private String message;

    public static OrganizationSignupResponse from(OrganizationSignupResult result) {
        Member member = result.getMember();
        Organization organization = result.getOrganization();
        
        return new OrganizationSignupResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getRole().name(),
                organization.getId(),
                organization.getName(),
                organization.getApprovalStatus().name(),
                "기관 회원가입이 완료되었습니다. 관리자 승인 후 서비스 이용이 가능합니다."
        );
    }
}
