package com.swcampus.api.mypage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "설문조사 등록/수정 요청")
public record SurveyRequest(
    @Schema(description = "전공", example = "컴퓨터공학")
    String major,

    @Schema(description = "부트캠프 수료 여부", example = "true")
    Boolean bootcampCompleted,

    @Schema(description = "희망 직무", example = "백엔드 개발자")
    String wantedJobs,

    @Schema(description = "보유 자격증", example = "정보처리기사")
    String licenses,

    @Schema(description = "국비지원 카드 보유 여부", example = "true")
    Boolean hasGovCard,

    @Schema(description = "수강 가능 금액", example = "500000")
    BigDecimal affordableAmount
) {
}
