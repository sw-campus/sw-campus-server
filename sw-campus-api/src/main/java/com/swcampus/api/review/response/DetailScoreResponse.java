package com.swcampus.api.review.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리별 점수 응답")
public record DetailScoreResponse(
    @Schema(description = "카테고리", example = "TEACHER")
    String category,

    @Schema(description = "점수", example = "4.5")
    Double score,

    @Schema(description = "카테고리별 후기", example = "강사님이 친절하고 설명을 잘 해주셨습니다.")
    String comment
) {}
