package com.swcampus.api.admin.response;

import com.swcampus.domain.survey.JobTypeCode;
import com.swcampus.domain.survey.SurveyOption;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 선택지 응답")
public record AdminOptionResponse(
        @Schema(description = "선택지 ID", example = "1")
        Long optionId,

        @Schema(description = "문항 ID", example = "1")
        Long questionId,

        @Schema(description = "선택지 순서", example = "1")
        Integer optionOrder,

        @Schema(description = "선택지 텍스트", example = "예")
        String optionText,

        @Schema(description = "선택지 값", example = "yes")
        String optionValue,

        @Schema(description = "점수 (Part 2용)", example = "5")
        Integer score,

        @Schema(description = "직무 타입 (Part 3용)", example = "F")
        JobTypeCode jobType,

        @Schema(description = "정답 여부 (Part 1용)", example = "true")
        Boolean isCorrect
) {
    public static AdminOptionResponse from(SurveyOption option) {
        return new AdminOptionResponse(
                option.getOptionId(),
                option.getQuestionId(),
                option.getOptionOrder(),
                option.getOptionText(),
                option.getOptionValue(),
                option.getScore(),
                option.getJobType(),
                option.getIsCorrect()
        );
    }
}
