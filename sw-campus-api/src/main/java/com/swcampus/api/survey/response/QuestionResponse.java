package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.QuestionPart;
import com.swcampus.domain.survey.QuestionType;
import com.swcampus.domain.survey.SurveyQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "문항 응답 (사용자용)")
public record QuestionResponse(
        @Schema(description = "문항 ID", example = "1")
        Long questionId,

        @Schema(description = "문항 순서", example = "1")
        Integer questionOrder,

        @Schema(description = "문항 텍스트", example = "다음 수열에서 빈칸에 들어갈 숫자는?")
        String questionText,

        @Schema(description = "문항 타입", example = "RADIO")
        QuestionType questionType,

        @Schema(description = "필수 여부", example = "true")
        boolean isRequired,

        @Schema(description = "필드 키", example = "q1")
        String fieldKey,

        @Schema(description = "파트 (성향 테스트용)", example = "PART1")
        QuestionPart part,

        @Schema(description = "선택지 목록")
        List<OptionResponse> options
) {
    public static QuestionResponse from(SurveyQuestion question) {
        List<OptionResponse> optionResponses = question.getOptions() != null
                ? question.getOptions().stream()
                    .map(OptionResponse::from)
                    .collect(Collectors.toList())
                : List.of();

        return new QuestionResponse(
                question.getQuestionId(),
                question.getQuestionOrder(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.isRequired(),
                question.getFieldKey(),
                question.getPart(),
                optionResponses
        );
    }
}
