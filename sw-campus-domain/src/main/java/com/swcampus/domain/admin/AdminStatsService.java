package com.swcampus.domain.admin;

import org.springframework.stereotype.Service;

import com.swcampus.domain.certificate.CertificateRepository;
import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.ApprovalStatus;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.review.ReviewRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 통계 서비스
 * 각 Repository의 count 메서드를 호출하여 통계 집계 수행
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final LectureRepository lectureRepository;
    private final CertificateRepository certificateRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 관리자 대시보드 통계 조회
     * 
     * @return 통계 데이터
     */
    public AdminStats getStats() {
        // 회원 통계
        long totalMembers = memberRepository.countAll();
        long userCount = memberRepository.countByRole(Role.USER);
        long organizationRoleCount = memberRepository.countByRole(Role.ORGANIZATION);
        long adminCount = memberRepository.countByRole(Role.ADMIN);

        // 기관 통계
        long totalOrganizations = organizationRepository.countAll();
        long pendingOrganizations = organizationRepository.countByApprovalStatus(ApprovalStatus.PENDING);

        // 강의 통계
        long totalLectures = lectureRepository.countAll();
        long pendingLectures = lectureRepository.countByAuthStatus(LectureAuthStatus.PENDING);

        // 수료증 통계
        long totalCertificates = certificateRepository.countAll();
        long pendingCertificates = certificateRepository.countByApprovalStatus(
                com.swcampus.domain.review.ApprovalStatus.PENDING);

        // 리뷰 통계 (수료증이 승인된 리뷰만 카운트 - 리뷰 관리는 수료증 승인 후 진행)
        long totalReviews = reviewRepository.countWithApprovedCertificate();
        long pendingReviews = reviewRepository.countWithApprovedCertificateAndReviewStatus(
                com.swcampus.domain.review.ApprovalStatus.PENDING);

        return AdminStats.builder()
                .totalMembers(totalMembers)
                .totalOrganizations(totalOrganizations)
                .totalLectures(totalLectures)
                .totalCertificates(totalCertificates)
                .totalReviews(totalReviews)
                .pendingOrganizations(pendingOrganizations)
                .pendingLectures(pendingLectures)
                .pendingCertificates(pendingCertificates)
                .pendingReviews(pendingReviews)
                .memberDistribution(AdminStats.MemberDistribution.builder()
                        .user(userCount)
                        .organization(organizationRoleCount)
                        .admin(adminCount)
                        .build())
                .build();
    }
}
