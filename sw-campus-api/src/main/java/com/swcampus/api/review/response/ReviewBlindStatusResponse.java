package com.swcampus.api.review.response;

import com.swcampus.domain.review.dto.ReviewBlindStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 블라인드 상태 응답")
public record ReviewBlindStatusResponse(
    @Schema(description = "블라인드 해제 여부", example = "false")
    boolean isUnblinded,

    @Schema(description = "승인된 리뷰 보유 여부", example = "false")
    boolean hasApprovedReview,

    @Schema(description = "설문조사 완료 여부", example = "false")
    boolean hasSurveyCompleted
) {
    public static ReviewBlindStatusResponse from(ReviewBlindStatus status) {
        return new ReviewBlindStatusResponse(
            status.isUnblinded(),
            status.hasApprovedReview(),
            status.hasSurveyCompleted()
        );
    }
}
