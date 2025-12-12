package com.swcampus.api.survey.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "설문조사 수정 요청")
public class UpdateSurveyRequest {

    @Schema(description = "전공", example = "소프트웨어공학")
    private String major;

    @Schema(description = "부트캠프 수료 여부", example = "true")
    private Boolean bootcampCompleted;

    @Schema(description = "희망 직무 (쉼표 구분)", example = "풀스택 개발자")
    private String wantedJobs;

    @Schema(description = "보유 자격증 (쉼표 구분)", example = "정보처리기사, SQLD, AWS SAA, CKAD")
    private String licenses;

    @Schema(description = "내일배움카드 보유 여부", example = "true")
    private Boolean hasGovCard;

    @Schema(description = "자비 부담 가능 금액", example = "1000000")
    private BigDecimal affordableAmount;
}
