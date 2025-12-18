package com.swcampus.domain.mypage.dto;

import com.swcampus.domain.review.ApprovalStatus;
import java.time.LocalDateTime;

/**
 * 내 후기 정보 DTO
 */
public record MyReviewInfo(
    Long reviewId,
    Long lectureId,
    String lectureName,
    Double score,
    String content,
    ApprovalStatus approvalStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean canEdit
) {
}
