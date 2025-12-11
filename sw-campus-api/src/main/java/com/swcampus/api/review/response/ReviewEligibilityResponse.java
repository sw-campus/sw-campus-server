package com.swcampus.api.review.response;

import com.swcampus.domain.review.ReviewEligibility;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "후기 작성 가능 여부 응답")
public record ReviewEligibilityResponse(
    @Schema(description = "닉네임 설정 여부", example = "true")
    boolean hasNickname,

    @Schema(description = "수료증 인증 여부", example = "true")
    boolean hasCertificate,

    @Schema(description = "후기 작성 가능 (기존 후기 없음)", example = "true")
    boolean canWrite,

    @Schema(description = "모든 조건 충족 여부", example = "true")
    boolean eligible,

    @Schema(description = "안내 메시지", example = "후기 작성이 가능합니다")
    String message
) {
    public static ReviewEligibilityResponse from(ReviewEligibility eligibility) {
        String message;
        if (!eligibility.hasNickname()) {
            message = "닉네임 설정이 필요합니다";
        } else if (!eligibility.hasCertificate()) {
            message = "수료증 인증이 필요합니다";
        } else if (!eligibility.canWrite()) {
            message = "이미 후기를 작성한 강의입니다";
        } else {
            message = "후기 작성이 가능합니다";
        }

        return new ReviewEligibilityResponse(
                eligibility.hasNickname(),
                eligibility.hasCertificate(),
                eligibility.canWrite(),
                eligibility.eligible(),
                message
        );
    }
}
