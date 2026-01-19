package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.MemberSurvey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "설문 상태 정보")
public record SurveyStatusResponse(
        @Schema(description = "기초 설문 완료 여부", example = "true")
        boolean hasBasicSurvey,

        @Schema(description = "성향 테스트 완료 여부", example = "false")
        boolean hasAptitudeTest,

        @Schema(description = "AI 기본 추천 사용 가능 여부", example = "true")
        boolean canUseBasicRecommendation,

        @Schema(description = "AI 정밀 추천 사용 가능 여부", example = "false")
        boolean canUsePreciseRecommendation
) {
    public static SurveyStatusResponse from(MemberSurvey survey) {
        if (survey == null) {
            return new SurveyStatusResponse(false, false, false, false);
        }
        return new SurveyStatusResponse(
                survey.hasBasicSurvey(),
                survey.hasAptitudeTest(),
                survey.canUseBasicRecommendation(),
                survey.canUsePreciseRecommendation()
        );
    }
}
