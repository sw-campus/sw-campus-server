package com.swcampus.api.mypage.response;

import com.swcampus.domain.survey.MemberSurvey;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "설문조사 응답")
public record SurveyResponse(
    @Schema(description = "설문조사 ID (Member ID)")
    Long surveyId,

    @Schema(description = "전공")
    String major,

    @Schema(description = "부트캠프 수료 여부")
    Boolean bootcampCompleted,

    @Schema(description = "희망 직무")
    String wantedJobs,

    @Schema(description = "보유 자격증")
    String licenses,

    @Schema(description = "국비지원 카드 보유 여부")
    Boolean hasGovCard,

    @Schema(description = "수강 가능 금액")
    BigDecimal affordableAmount,

    @Schema(description = "설문조사 존재 여부 (true: 이미 설문조사를 진행함, false: 설문조사 이력 없음)")
    Boolean exists
) {
    public static SurveyResponse empty() {
        return new SurveyResponse(null, null, null, null, null, null, null, false);
    }

    public static SurveyResponse from(MemberSurvey survey) {
        return new SurveyResponse(
            survey.getMemberId(),
            survey.getMajor(),
            survey.getBootcampCompleted(),
            survey.getWantedJobs(),
            survey.getLicenses(),
            survey.getHasGovCard(),
            survey.getAffordableAmount(),
            true
        );
    }
}
