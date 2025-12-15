package com.swcampus.domain.review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);

    Optional<Review> findById(Long id);

    Optional<Review> findByMemberIdAndLectureId(Long memberId, Long lectureId);

    boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);

    List<Review> findByLectureIdAndApprovalStatus(Long lectureId, ApprovalStatus status);

    List<Review> findByApprovalStatus(ApprovalStatus status);

    List<Review> findPendingReviews();

    Double getAverageScoreByLectureId(Long lectureId);
}
