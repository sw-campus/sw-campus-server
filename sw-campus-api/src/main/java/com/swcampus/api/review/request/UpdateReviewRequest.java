package com.swcampus.api.review.request;

import com.swcampus.domain.review.ReviewValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "후기 수정 요청")
public record UpdateReviewRequest(
    @Schema(description = "총평 (선택, 최대 " + ReviewValidationConstants.REVIEW_COMMENT_MAX_LENGTH + "자)", example = "수정된 후기 내용입니다.")
    @Size(max = ReviewValidationConstants.REVIEW_COMMENT_MAX_LENGTH, message = "총평은 최대 " + ReviewValidationConstants.REVIEW_COMMENT_MAX_LENGTH + "자입니다")
    String comment,

    @Schema(description = "카테고리별 상세 점수 (5개 모두 필수)")
    @NotNull(message = "상세 점수는 필수입니다")
    @Size(min = 5, max = 5, message = "상세 점수는 5개 카테고리 모두 필요합니다")
    @Valid
    List<DetailScoreRequest> detailScores
) {}
