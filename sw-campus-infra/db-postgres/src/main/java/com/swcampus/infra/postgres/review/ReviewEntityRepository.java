package com.swcampus.infra.postgres.review;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewRepository;
import com.swcampus.domain.review.exception.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewEntityRepository implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;

    @Override
    public Review save(Review review) {
        ReviewEntity entity;

        if (review.getId() != null) {
            // 기존 Entity 업데이트: 조회 후 값만 수정 (JPA Auditing 정상 작동)
            entity = jpaRepository.findByIdWithDetails(review.getId())
                    .orElseThrow(() -> new ReviewNotFoundException(review.getId()));
            entity.update(review);
        } else {
            // 신규 생성
            entity = ReviewEntity.from(review);
        }

        ReviewEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jpaRepository.findByIdWithDetails(id)
                .map(ReviewEntity::toDomain);
    }

    @Override
    public Optional<Review> findByMemberIdAndLectureId(Long memberId, Long lectureId) {
        return jpaRepository.findByMemberIdAndLectureIdWithDetails(memberId, lectureId)
                .map(ReviewEntity::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId) {
        return jpaRepository.existsByMemberIdAndLectureId(memberId, lectureId);
    }

    @Override
    public List<Review> findByLectureIdAndApprovalStatus(Long lectureId, ApprovalStatus status) {
        return jpaRepository.findByLectureIdAndApprovalStatusWithDetails(lectureId, status).stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByOrganizationIdAndApprovalStatus(Long organizationId, ApprovalStatus status) {
        return jpaRepository.findByOrganizationIdAndApprovalStatusWithDetails(organizationId, status).stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByApprovalStatus(ApprovalStatus status) {
        return jpaRepository.findByApprovalStatusWithDetails(status).stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public List<Review> findPendingReviews() {
        return jpaRepository.findByApprovalStatusWithDetails(ApprovalStatus.PENDING).stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public Double getAverageScoreByLectureId(Long lectureId) {
        return jpaRepository.findAverageScoreByLectureId(lectureId, ApprovalStatus.APPROVED);
    }

    @Override
    public Map<Long, Double> getAverageScoresByLectureIds(List<Long> lectureIds) {
        if (lectureIds == null || lectureIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Object[]> results = jpaRepository.findAverageScoresByLectureIds(lectureIds, ApprovalStatus.APPROVED);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).doubleValue()));
    }

    @Override
    public List<Review> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberIdWithDetails(memberId).stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public Long countReviewsByLectureId(Long lectureId) {
        return jpaRepository.countReviewsByLectureId(lectureId, ApprovalStatus.APPROVED);
    }

    @Override
    public Map<Long, Long> countReviewsByLectureIds(List<Long> lectureIds) {
        if (lectureIds == null || lectureIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Object[]> results = jpaRepository.countReviewsByLectureIds(lectureIds, ApprovalStatus.APPROVED);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
    }

    /**
     * 여러 강의의 리뷰 통계(평균점수, 리뷰수)를 한 번에 조회 (2 쿼리 → 1 쿼리 최적화)
     * 
     * @return Map<lectureId, Map<"avgScore"|"reviewCount", value>>
     */
    @Override
    public Map<Long, Map<String, Number>> getReviewStatsByLectureIds(List<Long> lectureIds) {
        if (lectureIds == null || lectureIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Object[]> results = jpaRepository.findReviewStatsByLectureIds(lectureIds, ApprovalStatus.APPROVED);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> Map.of(
                                "avgScore", (Number) row[1],
                                "reviewCount", (Number) row[2])));
    }

    @Override
    public Page<Review> findAllWithDetails(ApprovalStatus status, String keyword, Pageable pageable) {
        return jpaRepository.findAllWithDetailsAndKeyword(status, keyword, pageable)
                .map(ReviewEntity::toDomain);
    }

    @Override
    public Page<Review> findByOrganizationIdAndApprovalStatusWithPagination(Long organizationId, ApprovalStatus status,
            Pageable pageable) {
        return jpaRepository.findByOrganizationIdAndApprovalStatusWithPagination(organizationId, status, pageable)
                .map(ReviewEntity::toDomain);
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public long countByApprovalStatus(ApprovalStatus status) {
        return jpaRepository.countByApprovalStatus(status);
    }

    @Override
    public long countWithApprovedCertificate() {
        return jpaRepository.countWithApprovedCertificate();
    }

    @Override
    public long countWithApprovedCertificateAndReviewStatus(ApprovalStatus reviewStatus) {
        return jpaRepository.countWithApprovedCertificateAndReviewStatus(reviewStatus);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
