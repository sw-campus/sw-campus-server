package com.swcampus.infra.postgres.review;

import com.swcampus.domain.review.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
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
        ReviewEntity entity = ReviewEntity.from(review);
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
                        row -> (Double) row[1]));
    }

    @Override
    public List<Review> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberIdWithDetails(memberId).stream()
            .map(ReviewEntity::toDomain)
            .toList();
    }
}
