package com.swcampus.api.admin.response;

import java.util.List;

public record AdminReviewDetailResponse(
    Long reviewId,
    Long lectureId,
    String lectureName,
    Long memberId,
    String userName,
    String nickname,
    String comment,
    Double score,
    String approvalStatus,
    Long certificateId,
    String certificateApprovalStatus,
    List<DetailScore> detailScores,
    String createdAt
) {
    public record DetailScore(
        String category,
        Double score,
        String comment
    ) {}
}
