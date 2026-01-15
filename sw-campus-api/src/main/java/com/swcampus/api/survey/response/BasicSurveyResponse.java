package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기초 설문 응답")
public record BasicSurveyResponse(
        @Schema(description = "전공", example = "컴퓨터공학")
        String major,

        @Schema(description = "프로그래밍 경험 정보")
        ProgrammingExperienceResponse programmingExperience,

        @Schema(description = "선호 수업 방식", example = "OFFLINE")
        LearningMethod preferredLearningMethod,

        @Schema(description = "희망 직무 목록", example = "[\"BACKEND\", \"DATA\"]")
        List<DesiredJob> desiredJobs,

        @Schema(description = "희망 직무 기타 입력", example = "보안 엔지니어")
        String desiredJobOther,

        @Schema(description = "자비 부담 가능 금액 범위", example = "RANGE_100_200")
        BudgetRange affordableBudgetRange
) {
    public static BasicSurveyResponse from(BasicSurvey survey) {
        if (survey == null) return null;
        return new BasicSurveyResponse(
                survey.getMajor(),
                ProgrammingExperienceResponse.from(survey.getProgrammingExperience()),
                survey.getPreferredLearningMethod(),
                survey.getDesiredJobs(),
                survey.getDesiredJobOther(),
                survey.getAffordableBudgetRange()
        );
    }

    @Schema(description = "프로그래밍 경험 정보")
    public record ProgrammingExperienceResponse(
            @Schema(description = "경험 유무", example = "true")
            boolean hasExperience,

            @Schema(description = "부트캠프/교육과정명", example = "삼성 SW 아카데미")
            String bootcampName
    ) {
        public static ProgrammingExperienceResponse from(ProgrammingExperience experience) {
            if (experience == null) return null;
            return new ProgrammingExperienceResponse(
                    experience.isHasExperience(),
                    experience.getBootcampName()
            );
        }
    }
}
