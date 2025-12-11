package com.swcampus.api.admin.response;

import java.util.List;

public record AdminReviewListResponse(
    List<AdminReviewSummary> reviews,
    int totalCount
) {
    public record AdminReviewSummary(
        Long reviewId,
        Long lectureId,
        String lectureName,
        Long memberId,
        String userName,
        String nickname,
        Double score,
        Long certificateId,
        String certificateApprovalStatus,
        String reviewApprovalStatus,
        String createdAt
    ) {}
}
