package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyResults {
    /**
     * 적성 점수 (Part1 + Part2, 0-80점) - 내부 활용 전용
     */
    private Integer aptitudeScore;

    /**
     * 적성 등급 - 내부 활용 전용 (사용자 노출 X)
     */
    private AptitudeGrade aptitudeGrade;

    /**
     * Part 3 직무 유형별 카운트
     */
    private Map<JobTypeCode, Integer> jobTypeScores;

    /**
     * 추천 직무 - 사용자에게 표시되는 유일한 결과
     */
    private RecommendedJob recommendedJob;

    @Builder
    public SurveyResults(
            Integer aptitudeScore,
            AptitudeGrade aptitudeGrade,
            Map<JobTypeCode, Integer> jobTypeScores,
            RecommendedJob recommendedJob
    ) {
        this.aptitudeScore = aptitudeScore;
        this.aptitudeGrade = aptitudeGrade;
        this.jobTypeScores = jobTypeScores;
        this.recommendedJob = recommendedJob;
    }
}
