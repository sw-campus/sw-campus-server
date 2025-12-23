package com.swcampus.api.admin.response;

import com.swcampus.domain.admin.AdminStats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 대시보드 통계 응답 DTO
 */
@Getter
@Builder
@Schema(description = "관리자 대시보드 통계 응답")
public class AdminStatsResponse {

    @Schema(description = "전체 회원 수", example = "100")
    private final long totalMembers;

    @Schema(description = "전체 기관 수", example = "20")
    private final long totalOrganizations;

    @Schema(description = "전체 강의 수", example = "50")
    private final long totalLectures;

    @Schema(description = "전체 수료증 수", example = "30")
    private final long totalCertificates;

    @Schema(description = "전체 리뷰 수", example = "45")
    private final long totalReviews;

    @Schema(description = "승인 대기 기관 수", example = "5")
    private final long pendingOrganizations;

    @Schema(description = "승인 대기 강의 수", example = "3")
    private final long pendingLectures;

    @Schema(description = "승인 대기 수료증 수", example = "2")
    private final long pendingCertificates;

    @Schema(description = "승인 대기 리뷰 수", example = "4")
    private final long pendingReviews;

    @Schema(description = "회원 역할별 분포")
    private final MemberDistributionResponse memberDistribution;

    @Getter
    @Builder
    @Schema(description = "회원 역할별 분포")
    public static class MemberDistributionResponse {
        @Schema(description = "일반 회원 수", example = "70")
        private final long user;

        @Schema(description = "기관 회원 수", example = "25")
        private final long organization;

        @Schema(description = "관리자 수", example = "5")
        private final long admin;

        public static MemberDistributionResponse from(AdminStats.MemberDistribution distribution) {
            return MemberDistributionResponse.builder()
                    .user(distribution.getUser())
                    .organization(distribution.getOrganization())
                    .admin(distribution.getAdmin())
                    .build();
        }
    }

    public static AdminStatsResponse from(AdminStats stats) {
        return AdminStatsResponse.builder()
                .totalMembers(stats.getTotalMembers())
                .totalOrganizations(stats.getTotalOrganizations())
                .totalLectures(stats.getTotalLectures())
                .totalCertificates(stats.getTotalCertificates())
                .totalReviews(stats.getTotalReviews())
                .pendingOrganizations(stats.getPendingOrganizations())
                .pendingLectures(stats.getPendingLectures())
                .pendingCertificates(stats.getPendingCertificates())
                .pendingReviews(stats.getPendingReviews())
                .memberDistribution(MemberDistributionResponse.from(stats.getMemberDistribution()))
                .build();
    }
}
