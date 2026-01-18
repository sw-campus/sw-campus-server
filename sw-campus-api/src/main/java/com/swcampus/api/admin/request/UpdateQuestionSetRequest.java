package com.swcampus.api.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "문항 세트 수정 요청")
public record UpdateQuestionSetRequest(
        @Schema(description = "문항 세트명", example = "기초 설문 v2 수정", required = true)
        @NotBlank(message = "문항 세트명은 필수입니다")
        String name,

        @Schema(description = "설명", example = "기초 설문 개선 버전 수정")
        String description
) {
}
