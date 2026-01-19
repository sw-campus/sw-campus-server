package com.swcampus.api.admin.request;

import com.swcampus.domain.survey.JobTypeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "선택지 생성 요청 (값은 Part에 따라 자동 생성됨)")
public record CreateOptionRequest(
        @Schema(description = "선택지 텍스트", example = "예", required = true)
        @NotBlank(message = "선택지 텍스트는 필수입니다")
        String optionText,

        @Schema(description = "점수 (Part 2용)", example = "5")
        Integer score,

        @Schema(description = "직무 타입 (Part 3용)", example = "F")
        JobTypeCode jobType,

        @Schema(description = "정답 여부 (Part 1용)", example = "true")
        Boolean isCorrect
) {
}
