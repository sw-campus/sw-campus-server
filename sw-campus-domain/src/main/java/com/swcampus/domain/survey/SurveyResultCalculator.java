package com.swcampus.domain.survey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 성향 테스트 점수 계산기.
 * 정답은 서버에서만 관리되며, 사용자 API에는 노출되지 않음.
 */
@Slf4j
@Component
public class SurveyResultCalculator {

    /**
     * 성향 테스트 응답과 문항 세트를 기반으로 결과 계산
     */
    public SurveyResults calculate(AptitudeTest test, SurveyQuestionSet questionSet) {
        int part1Score = 0;
        int part2Score = 0;
        Map<JobTypeCode, Integer> jobScores = new EnumMap<>(JobTypeCode.class);

        // 초기화
        for (JobTypeCode code : JobTypeCode.values()) {
            jobScores.put(code, 0);
        }

        for (SurveyQuestion question : questionSet.getQuestions()) {
            if (question.getPart() == null) continue;

            switch (question.getPart()) {
                case PART1 -> part1Score += calculatePart1Score(test, question);
                case PART2 -> part2Score += calculatePart2Score(test, question);
                case PART3 -> updateJobScores(test, question, jobScores);
            }
        }

        int totalScore = part1Score + part2Score;
        AptitudeGrade grade = AptitudeGrade.fromScore(totalScore);
        RecommendedJob recommendedJob = determineRecommendedJob(jobScores);

        return SurveyResults.builder()
                .aptitudeScore(totalScore)
                .aptitudeGrade(grade)
                .jobTypeScores(jobScores)
                .recommendedJob(recommendedJob)
                .build();
    }

    private int calculatePart1Score(AptitudeTest test, SurveyQuestion question) {
        String key = question.getFieldKey();
        Integer answer = test.getPart1Answers().get(key);

        if (answer == null) {
            log.warn("Part 1 응답 누락: questionKey={}", key);
            return 0;
        }

        SurveyOption selectedOption = findOptionByOrder(question, answer);
        if (selectedOption == null) {
            log.warn("Part 1 선택지 불일치: questionKey={}, answer={}", key, answer);
            return 0;
        }
        if (Boolean.TRUE.equals(selectedOption.getIsCorrect())) {
            return 10;
        }
        return 0;
    }

    private int calculatePart2Score(AptitudeTest test, SurveyQuestion question) {
        String key = question.getFieldKey();
        Integer answer = test.getPart2Answers().get(key);

        if (answer == null) {
            log.warn("Part 2 응답 누락: questionKey={}", key);
            return 0;
        }

        SurveyOption selectedOption = findOptionByOrder(question, answer);
        if (selectedOption == null) {
            log.warn("Part 2 선택지 불일치: questionKey={}, answer={}", key, answer);
            return 0;
        }
        return selectedOption.getScore();
    }

    private void updateJobScores(AptitudeTest test, SurveyQuestion question, Map<JobTypeCode, Integer> jobScores) {
        String key = question.getFieldKey();
        String answer = test.getPart3Answers().get(key);

        if (answer == null) return;

        try {
            JobTypeCode jobType = JobTypeCode.valueOf(answer);
            jobScores.merge(jobType, 1, Integer::sum);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid JobTypeCode for question {}: {}", key, answer);
        }
    }

    private SurveyOption findOptionByOrder(SurveyQuestion question, int order) {
        return question.getOptions().stream()
                .filter(o -> o.getOptionOrder().equals(order))
                .findFirst()
                .orElse(null);
    }

    private RecommendedJob determineRecommendedJob(Map<JobTypeCode, Integer> jobScores) {
        int maxCount = 0;
        JobTypeCode maxType = null;
        int maxCountTypes = 0;

        for (Map.Entry<JobTypeCode, Integer> entry : jobScores.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxType = entry.getKey();
                maxCountTypes = 1;
            } else if (entry.getValue() == maxCount && maxCount > 0) {
                maxCountTypes++;
            }
        }

        // 동점 시 FULLSTACK
        if (maxCountTypes > 1) {
            return RecommendedJob.FULLSTACK;
        }

        if (maxType != null) {
            return maxType.toRecommendedJob();
        }

        return RecommendedJob.FULLSTACK;
    }
}
