package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.MemberSurvey;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "설문조사 전체 응답")
public record SurveyResponse(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "기초 설문 데이터")
        BasicSurveyResponse basicSurvey,

        @Schema(description = "설문 결과 (추천 직무)")
        SurveyResultsResponse results,

        @Schema(description = "설문 상태")
        SurveyStatusResponse status,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt,

        @Schema(description = "완료일시")
        LocalDateTime completedAt
) {
    public static SurveyResponse from(MemberSurvey survey) {
        if (survey == null) {
            return null;
        }
        return new SurveyResponse(
                survey.getMemberId(),
                BasicSurveyResponse.from(survey.getBasicSurvey()),
                SurveyResultsResponse.from(survey.getResults()),
                SurveyStatusResponse.from(survey),
                survey.getCreatedAt(),
                survey.getUpdatedAt(),
                survey.getCompletedAt()
        );
    }
}
