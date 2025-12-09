package com.swcampus.api.auth.response;

import com.swcampus.domain.auth.OrganizationSignupResult;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.organization.Organization;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "기관 회원가입 응답")
public class OrganizationSignupResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "provider@company.com")
    private String email;

    @Schema(description = "이름", example = "김대표")
    private String name;

    @Schema(description = "닉네임", example = "대표님")
    private String nickname;

    @Schema(description = "권한", example = "PROVIDER")
    private String role;

    @Schema(description = "기관 ID", example = "1")
    private Long organizationId;

    @Schema(description = "기관명", example = "ABC교육원")
    private String organizationName;

    @Schema(description = "승인 상태", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private String approvalStatus;

    @Schema(description = "안내 메시지", example = "기관 회원가입이 완료되었습니다. 관리자 승인 후 서비스 이용이 가능합니다.")
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
