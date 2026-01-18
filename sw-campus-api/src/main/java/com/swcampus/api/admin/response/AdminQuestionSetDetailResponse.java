package com.swcampus.api.admin.response;

import com.swcampus.domain.survey.QuestionSetStatus;
import com.swcampus.domain.survey.QuestionSetType;
import com.swcampus.domain.survey.SurveyQuestionSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "관리자 문항 세트 상세 응답 (문항 포함)")
public record AdminQuestionSetDetailResponse(
        @Schema(description = "문항 세트 ID", example = "1")
        Long questionSetId,

        @Schema(description = "문항 세트명", example = "기초 설문 v1")
        String name,

        @Schema(description = "설명", example = "기초 설문 첫 번째 버전")
        String description,

        @Schema(description = "세트 타입", example = "BASIC")
        QuestionSetType type,

        @Schema(description = "버전", example = "1")
        Integer version,

        @Schema(description = "상태", example = "DRAFT")
        QuestionSetStatus status,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt,

        @Schema(description = "발행일시")
        LocalDateTime publishedAt,

        @Schema(description = "문항 목록")
        List<AdminQuestionResponse> questions
) {
    public static AdminQuestionSetDetailResponse from(SurveyQuestionSet questionSet) {
        List<AdminQuestionResponse> questionResponses = questionSet.getQuestions() != null
                ? questionSet.getQuestions().stream()
                    .map(AdminQuestionResponse::from)
                    .collect(Collectors.toList())
                : List.of();

        return new AdminQuestionSetDetailResponse(
                questionSet.getQuestionSetId(),
                questionSet.getName(),
                questionSet.getDescription(),
                questionSet.getType(),
                questionSet.getVersion(),
                questionSet.getStatus(),
                questionSet.getCreatedAt(),
                questionSet.getUpdatedAt(),
                questionSet.getPublishedAt(),
                questionResponses
        );
    }
}
