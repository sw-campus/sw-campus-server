package com.swcampus.api.survey.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "설문조사 작성 요청")
public class CreateSurveyRequest {

    @Schema(description = "전공", example = "컴퓨터공학")
    private String major;

    @Schema(description = "부트캠프 수료 여부", example = "true")
    private Boolean bootcampCompleted;

    @Schema(description = "희망 직무 (쉼표 구분)", example = "백엔드 개발자, 데이터 엔지니어")
    private String wantedJobs;

    @Schema(description = "보유 자격증 (쉼표 구분)", example = "정보처리기사, SQLD, AWS SAA")
    private String licenses;

    @Schema(description = "내일배움카드 보유 여부", example = "true")
    private Boolean hasGovCard;

    @Schema(description = "자비 부담 가능 금액", example = "500000")
    private BigDecimal affordableAmount;
}
