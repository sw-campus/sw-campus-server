package com.swcampus.domain.review.dto;

/**
 * 리뷰 블라인드 해제 상태를 나타내는 DTO.
 * 
 * @param isUnblinded 블라인드 해제 여부 (hasApprovedReview OR hasSurveyCompleted)
 * @param hasApprovedReview 승인된 리뷰 보유 여부
 * @param hasSurveyCompleted 설문조사 완료 여부
 */
public record ReviewBlindStatus(
    boolean isUnblinded,
    boolean hasApprovedReview,
    boolean hasSurveyCompleted
) {
    public static ReviewBlindStatus of(boolean hasApprovedReview, boolean hasSurveyCompleted) {
        return new ReviewBlindStatus(
            hasApprovedReview || hasSurveyCompleted,
            hasApprovedReview,
            hasSurveyCompleted
        );
    }
}
