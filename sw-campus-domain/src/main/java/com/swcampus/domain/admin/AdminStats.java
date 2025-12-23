package com.swcampus.domain.admin;

import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 대시보드 통계 도메인 모델
 */
@Getter
@Builder
public class AdminStats {
    private final long totalMembers;
    private final long totalOrganizations;
    private final long totalLectures;
    private final long totalCertificates;
    private final long totalReviews;
    
    private final long pendingOrganizations;
    private final long pendingLectures;
    private final long pendingCertificates;
    private final long pendingReviews;
    
    private final MemberDistribution memberDistribution;
    
    @Getter
    @Builder
    public static class MemberDistribution {
        private final long user;
        private final long organization;
        private final long admin;
    }
}
