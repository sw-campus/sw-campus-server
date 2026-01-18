package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.SurveyOption;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택지 응답 (사용자용) - 채점 정보 제외")
public record OptionResponse(
        @Schema(description = "선택지 ID", example = "1")
        Long optionId,

        @Schema(description = "선택지 순서", example = "1")
        Integer optionOrder,

        @Schema(description = "선택지 텍스트", example = "42")
        String optionText,

        @Schema(description = "선택지 값 (제출 시 사용)", example = "1")
        String optionValue
) {
    public static OptionResponse from(SurveyOption option) {
        return new OptionResponse(
                option.getOptionId(),
                option.getOptionOrder(),
                option.getOptionText(),
                option.getOptionValue()
        );
    }
}
