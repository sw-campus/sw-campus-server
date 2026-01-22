package com.swcampus.api.review.response;

import java.util.List;

import com.swcampus.domain.review.dto.ReviewListResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 목록 응답 (블라인드 정보 포함)")
public record ReviewListResponse(
    @Schema(description = "리뷰 목록")
    List<ReviewResponse> reviews,

    @Schema(description = "전체 리뷰 개수 (블라인드 무관)", example = "15")
    int totalCount,

    @Schema(description = "블라인드 해제 여부", example = "false")
    boolean isUnblinded
) {
    public static ReviewListResponse from(ReviewListResult result) {
        List<ReviewResponse> reviews = result.reviews().stream()
            .map(rwn -> ReviewResponse.from(rwn.review(), rwn.nickname()))
            .toList();
        
        return new ReviewListResponse(
            reviews,
            result.totalCount(),
            result.isUnblinded()
        );
    }
}
