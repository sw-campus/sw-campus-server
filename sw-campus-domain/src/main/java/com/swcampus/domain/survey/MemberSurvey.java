package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSurvey {
    private Long memberId;
    private BasicSurvey basicSurvey;
    private AptitudeTest aptitudeTest;
    private SurveyResults results;
    private AptitudeGrade aptitudeGrade;
    private RecommendedJob recommendedJob;
    private Integer aptitudeScore;
    private Integer questionSetVersion;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public MemberSurvey(
            Long memberId,
            BasicSurvey basicSurvey,
            AptitudeTest aptitudeTest,
            SurveyResults results,
            AptitudeGrade aptitudeGrade,
            RecommendedJob recommendedJob,
            Integer aptitudeScore,
            Integer questionSetVersion,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.memberId = memberId;
        this.basicSurvey = basicSurvey;
        this.aptitudeTest = aptitudeTest;
        this.results = results;
        this.aptitudeGrade = aptitudeGrade;
        this.recommendedJob = recommendedJob;
        this.aptitudeScore = aptitudeScore;
        this.questionSetVersion = questionSetVersion;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MemberSurvey createWithBasicSurvey(Long memberId, BasicSurvey basicSurvey) {
        return MemberSurvey.builder()
                .memberId(memberId)
                .basicSurvey(basicSurvey)
                .build();
    }

    public void updateBasicSurvey(BasicSurvey basicSurvey) {
        this.basicSurvey = basicSurvey;
    }

    public void completeAptitudeTest(
            AptitudeTest aptitudeTest,
            SurveyResults results,
            Integer questionSetVersion
    ) {
        this.aptitudeTest = aptitudeTest;
        this.results = results;
        this.aptitudeGrade = results.getAptitudeGrade();
        this.recommendedJob = results.getRecommendedJob();
        this.aptitudeScore = results.getAptitudeScore();
        this.questionSetVersion = questionSetVersion;
        this.completedAt = LocalDateTime.now();
    }

    public boolean hasBasicSurvey() {
        return basicSurvey != null;
    }

    public boolean hasAptitudeTest() {
        return aptitudeTest != null;
    }

    public boolean isComplete() {
        return completedAt != null;
    }

    /**
     * AI 기본 추천 가능 여부
     */
    public boolean canUseBasicRecommendation() {
        return hasBasicSurvey();
    }

    /**
     * AI 정밀 추천 가능 여부
     */
    public boolean canUsePreciseRecommendation() {
        return hasAptitudeTest();
    }
}
