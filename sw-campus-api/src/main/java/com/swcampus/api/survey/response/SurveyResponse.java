package com.swcampus.api.survey.response;

import com.swcampus.domain.survey.MemberSurvey;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "설문조사 응답")
public record SurveyResponse(
        @Schema(description = "회원 ID", example = "1")
        Long userId,

        @Schema(description = "전공", example = "컴퓨터공학")
        String major,

        @Schema(description = "부트캠프 수료 여부", example = "true")
        Boolean bootcampCompleted,

        @Schema(description = "희망 직무", example = "백엔드 개발자, 데이터 엔지니어")
        String wantedJobs,

        @Schema(description = "보유 자격증", example = "정보처리기사, SQLD, AWS SAA")
        String licenses,

        @Schema(description = "내일배움카드 보유 여부", example = "true")
        Boolean hasGovCard,

        @Schema(description = "자비 부담 가능 금액", example = "500000")
        BigDecimal affordableAmount,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static SurveyResponse from(MemberSurvey survey) {
        return new SurveyResponse(
                survey.getUserId(),
                survey.getMajor(),
                survey.getBootcampCompleted(),
                survey.getWantedJobs(),
                survey.getLicenses(),
                survey.getHasGovCard(),
                survey.getAffordableAmount(),
                survey.getCreatedAt(),
                survey.getUpdatedAt()
        );
    }
}
