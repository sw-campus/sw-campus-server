package com.swcampus.api.mypage.response;

import com.swcampus.domain.survey.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * @deprecated Use /api/v1/members/me/survey endpoint instead
 */
@Deprecated
@Schema(description = "설문조사 응답 (Deprecated - /api/v1/members/me/survey 사용 권장)")
public record SurveyResponse(
    @Schema(description = "설문조사 ID (= Member ID)")
    Long surveyId,

    @Schema(description = "전공")
    String major,

    @Schema(description = "프로그래밍 경험 유무")
    Boolean hasProgrammingExperience,

    @Schema(description = "부트캠프/교육과정명")
    String bootcampName,

    @Schema(description = "선호 수업 방식")
    LearningMethod preferredLearningMethod,

    @Schema(description = "희망 직무 목록")
    List<DesiredJob> desiredJobs,

    @Schema(description = "희망 직무 기타")
    String desiredJobOther,

    @Schema(description = "자비 부담 가능 금액 범위")
    BudgetRange affordableBudgetRange,

    @Schema(description = "설문조사 존재 여부 (true: 이미 설문조사를 진행함, false: 설문조사 이력 없음)")
    Boolean exists
) {
    public static SurveyResponse empty() {
        return new SurveyResponse(null, null, null, null, null, null, null, null, false);
    }

    public static SurveyResponse from(MemberSurvey survey) {
        BasicSurvey basicSurvey = survey.getBasicSurvey();
        if (basicSurvey == null) {
            return new SurveyResponse(survey.getMemberId(), null, null, null, null, null, null, null, true);
        }

        ProgrammingExperience exp = basicSurvey.getProgrammingExperience();
        return new SurveyResponse(
            survey.getMemberId(),
            basicSurvey.getMajor(),
            exp != null ? exp.isHasExperience() : null,
            exp != null ? exp.getBootcampName() : null,
            basicSurvey.getPreferredLearningMethod(),
            basicSurvey.getDesiredJobs(),
            basicSurvey.getDesiredJobOther(),
            basicSurvey.getAffordableBudgetRange(),
            true
        );
    }
}
