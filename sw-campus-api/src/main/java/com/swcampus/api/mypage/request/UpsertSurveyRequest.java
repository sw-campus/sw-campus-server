package com.swcampus.api.mypage.request;

import com.swcampus.domain.survey.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * @deprecated Use /api/v1/members/me/survey/basic endpoint instead
 */
@Deprecated
@Schema(description = "설문조사 등록/수정 요청 (Deprecated - /api/v1/members/me/survey/basic 사용 권장)")
public record UpsertSurveyRequest(
    @Schema(description = "전공", example = "컴퓨터공학")
    String major,

    @Schema(description = "프로그래밍 경험 유무", example = "true")
    Boolean hasProgrammingExperience,

    @Schema(description = "부트캠프/교육과정명 (경험이 있는 경우)", example = "삼성 SW 아카데미")
    String bootcampName,

    @Schema(description = "선호 수업 방식", example = "OFFLINE")
    LearningMethod preferredLearningMethod,

    @Schema(description = "희망 직무 목록", example = "[\"BACKEND\", \"DATA\"]")
    List<DesiredJob> desiredJobs,

    @Schema(description = "희망 직무 기타 입력", example = "보안 엔지니어")
    String desiredJobOther,

    @Schema(description = "자비 부담 가능 금액 범위", example = "RANGE_100_200")
    BudgetRange affordableBudgetRange
) {
    public BasicSurvey toBasicSurvey() {
        ProgrammingExperience experience = Boolean.TRUE.equals(hasProgrammingExperience)
                ? ProgrammingExperience.withExperience(bootcampName)
                : ProgrammingExperience.noExperience();

        return BasicSurvey.builder()
                .major(major)
                .programmingExperience(experience)
                .preferredLearningMethod(preferredLearningMethod)
                .desiredJobs(desiredJobs)
                .desiredJobOther(desiredJobOther)
                .affordableBudgetRange(affordableBudgetRange)
                .build();
    }
}
