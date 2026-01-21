package com.swcampus.domain.review.dto;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.review.Review;
import com.swcampus.domain.review.ReviewDetail;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 대기 중인 후기 정보 DTO
 */
public record PendingReviewInfo(
    Long reviewId,
    Long lectureId,
    String lectureName,
    Long memberId,
    String userName,
    String nickname,
    Double score,
    String comment,
    Long certificateId,
    ApprovalStatus certificateApprovalStatus,
    ApprovalStatus reviewApprovalStatus,
    List<ReviewDetail> details,
    LocalDateTime createdAt
) {
    public static PendingReviewInfo of(
            Review review,
            String lectureName,
            String userName,
            String nickname,
            ApprovalStatus certificateApprovalStatus
    ) {
        return new PendingReviewInfo(
            review.getId(),
            review.getLectureId(),
            lectureName,
            review.getMemberId(),
            userName,
            nickname,
            review.getScore(),
            review.getComment(),
            review.getCertificateId(),
            certificateApprovalStatus,
            review.getApprovalStatus(),
            review.getDetails(),
            review.getCreatedAt()
        );
    }
}
