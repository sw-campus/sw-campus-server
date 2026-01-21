package com.swcampus.api.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원 역할별 통계 응답 DTO
 */
@Getter
@Builder
@Schema(description = "회원 역할별 통계 응답")
public class MemberRoleStatsResponse {

    @Schema(description = "전체 회원 수", example = "100")
    private final long total;

    @Schema(description = "일반 회원 수", example = "70")
    private final long user;

    @Schema(description = "기관 회원 수", example = "25")
    private final long organization;

    @Schema(description = "관리자 수", example = "5")
    private final long admin;

    public static MemberRoleStatsResponse of(long total, long user, long organization, long admin) {
        return MemberRoleStatsResponse.builder()
                .total(total)
                .user(user)
                .organization(organization)
                .admin(admin)
                .build();
    }
}
