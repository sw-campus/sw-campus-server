package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.RecommendedJob;
import com.swcampus.domain.survey.SurveyResults;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 설문 결과 응답 - 사용자에게는 추천 직무만 노출
 */
@Schema(description = "설문 결과 응답")
public record SurveyResultsResponse(
        @Schema(description = "추천 직무", example = "BACKEND")
        RecommendedJob recommendedJob,

        @Schema(description = "추천 직무 설명", example = "백엔드 개발자")
        String recommendedJobDescription
) {
    public static SurveyResultsResponse from(SurveyResults results) {
        if (results == null) return null;
        return new SurveyResultsResponse(
                results.getRecommendedJob(),
                results.getRecommendedJob() != null ? results.getRecommendedJob().getDescription() : null
        );
    }
}
