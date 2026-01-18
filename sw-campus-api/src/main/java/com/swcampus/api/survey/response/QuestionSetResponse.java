package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.QuestionSetType;
import com.swcampus.domain.survey.SurveyQuestionSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "발행된 문항 세트 응답")
public record QuestionSetResponse(
        @Schema(description = "문항 세트 ID", example = "1")
        Long questionSetId,

        @Schema(description = "문항 세트명", example = "성향 테스트 v2")
        String name,

        @Schema(description = "설명", example = "성향 테스트 문항입니다.")
        String description,

        @Schema(description = "세트 타입", example = "APTITUDE")
        QuestionSetType type,

        @Schema(description = "버전", example = "2")
        Integer version,

        @Schema(description = "문항 목록")
        List<QuestionResponse> questions
) {
    public static QuestionSetResponse from(SurveyQuestionSet questionSet) {
        List<QuestionResponse> questionResponses = questionSet.getQuestions() != null
                ? questionSet.getQuestions().stream()
                    .map(QuestionResponse::from)
                    .collect(Collectors.toList())
                : List.of();

        return new QuestionSetResponse(
                questionSet.getQuestionSetId(),
                questionSet.getName(),
                questionSet.getDescription(),
                questionSet.getType(),
                questionSet.getVersion(),
                questionResponses
        );
    }
}
