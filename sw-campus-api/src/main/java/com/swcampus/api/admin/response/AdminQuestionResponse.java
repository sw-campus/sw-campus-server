package com.swcampus.api.admin.response;

import com.swcampus.domain.survey.QuestionPart;
import com.swcampus.domain.survey.QuestionType;
import com.swcampus.domain.survey.SurveyQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "관리자 문항 응답")
public record AdminQuestionResponse(
        @Schema(description = "문항 ID", example = "1")
        Long questionId,

        @Schema(description = "문항 세트 ID", example = "1")
        Long questionSetId,

        @Schema(description = "문항 순서", example = "1")
        Integer questionOrder,

        @Schema(description = "문항 텍스트", example = "개발 경험이 있으신가요?")
        String questionText,

        @Schema(description = "문항 타입", example = "RADIO")
        QuestionType questionType,

        @Schema(description = "필수 여부", example = "true")
        boolean isRequired,

        @Schema(description = "필드 키", example = "has_experience")
        String fieldKey,

        @Schema(description = "파트 (성향 테스트용)", example = "PART1")
        QuestionPart part,

        @Schema(description = "표시 조건 (JSON)")
        Map<String, Object> showCondition,

        @Schema(description = "메타데이터 (JSON)")
        Map<String, Object> metadata,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "선택지 목록")
        List<AdminOptionResponse> options
) {
    public static AdminQuestionResponse from(SurveyQuestion question) {
        List<AdminOptionResponse> optionResponses = question.getOptions() != null
                ? question.getOptions().stream()
                    .map(AdminOptionResponse::from)
                    .collect(Collectors.toList())
                : List.of();

        return new AdminQuestionResponse(
                question.getQuestionId(),
                question.getQuestionSetId(),
                question.getQuestionOrder(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.isRequired(),
                question.getFieldKey(),
                question.getPart(),
                question.getShowCondition(),
                question.getMetadata(),
                question.getCreatedAt(),
                optionResponses
        );
    }
}
