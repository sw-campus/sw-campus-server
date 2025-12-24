package com.swcampus.domain.review;

import com.swcampus.domain.common.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);

    Optional<Review> findById(Long id);

    Optional<Review> findByMemberIdAndLectureId(Long memberId, Long lectureId);

    boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);

    List<Review> findByLectureIdAndApprovalStatus(Long lectureId, ApprovalStatus status);

    List<Review> findByOrganizationIdAndApprovalStatus(Long organizationId, ApprovalStatus status);

    List<Review> findByApprovalStatus(ApprovalStatus status);

    List<Review> findPendingReviews();

    List<Review> findAllByMemberId(Long memberId);

    Double getAverageScoreByLectureId(Long lectureId);

    Map<Long, Double> getAverageScoresByLectureIds(List<Long> lectureIds);

    Long countReviewsByLectureId(Long lectureId);

    Map<Long, Long> countReviewsByLectureIds(List<Long> lectureIds);

    Page<Review> findAllWithDetails(ApprovalStatus status, String keyword, Pageable pageable);

    Page<Review> findByOrganizationIdAndApprovalStatusWithPagination(Long organizationId, ApprovalStatus status, Pageable pageable);

    // Statistics methods
    long countAll();
    long countByApprovalStatus(ApprovalStatus status);

    /**
     * 수료증이 승인된 리뷰 수를 조회합니다.
     * (리뷰 관리는 수료증 승인 후에 진행되므로, 실제 '리뷰' 카운트에 사용)
     */
    long countWithApprovedCertificate();

    /**
     * 수료증이 승인되고 특정 리뷰 상태인 리뷰 수를 조회합니다.
     */
    long countWithApprovedCertificateAndReviewStatus(ApprovalStatus reviewStatus);

    void deleteById(Long id);
}
