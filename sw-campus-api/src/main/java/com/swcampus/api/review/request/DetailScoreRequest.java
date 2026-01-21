package com.swcampus.api.review.request;

import com.swcampus.domain.review.ReviewValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "카테고리별 점수 요청")
public record DetailScoreRequest(
    @Schema(description = "카테고리", example = "TEACHER", 
            allowableValues = {"TEACHER", "CURRICULUM", "MANAGEMENT", "FACILITY", "PROJECT"})
    @NotNull(message = "카테고리는 필수입니다")
    String category,

    @Schema(description = "점수 (" + ReviewValidationConstants.SCORE_MIN + " ~ " + ReviewValidationConstants.SCORE_MAX + ")", example = "4.5")
    @NotNull(message = "점수는 필수입니다")
    @DecimalMin(value = ReviewValidationConstants.SCORE_MIN, message = "점수는 " + ReviewValidationConstants.SCORE_MIN + " 이상이어야 합니다")
    @DecimalMax(value = ReviewValidationConstants.SCORE_MAX, message = "점수는 " + ReviewValidationConstants.SCORE_MAX + " 이하여야 합니다")
    Double score,

    @Schema(description = "카테고리별 후기 (" + ReviewValidationConstants.DETAIL_COMMENT_MIN_LENGTH + "~" + ReviewValidationConstants.DETAIL_COMMENT_MAX_LENGTH + "자)", example = "강사님이 친절하고 설명을 잘 해주셔서 이해하기 쉬웠습니다.")
    @NotBlank(message = "카테고리별 후기는 필수입니다")
    @Size(min = ReviewValidationConstants.DETAIL_COMMENT_MIN_LENGTH, max = ReviewValidationConstants.DETAIL_COMMENT_MAX_LENGTH, message = "카테고리별 후기는 " + ReviewValidationConstants.DETAIL_COMMENT_MIN_LENGTH + "~" + ReviewValidationConstants.DETAIL_COMMENT_MAX_LENGTH + "자입니다")
    String comment
) {}
