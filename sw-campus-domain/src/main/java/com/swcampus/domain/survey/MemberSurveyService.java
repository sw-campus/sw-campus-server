package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.AptitudeTestRequiredException;
import com.swcampus.domain.survey.exception.BasicSurveyRequiredException;
import com.swcampus.domain.survey.exception.InvalidAptitudeTestAnswersException;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import com.swcampus.domain.survey.exception.SurveyQuestionSetNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSurveyService {

    private final MemberSurveyRepository surveyRepository;
    private final SurveyQuestionSetRepository questionSetRepository;
    private final SurveyResultCalculator resultCalculator;

    /**
     * 기초 설문 저장 (Upsert)
     */
    @Transactional
    public MemberSurvey saveBasicSurvey(Long memberId, BasicSurvey basicSurvey) {
        return surveyRepository.findByMemberId(memberId)
                .map(survey -> {
                    survey.updateBasicSurvey(basicSurvey);
                    return surveyRepository.save(survey);
                })
                .orElseGet(() -> {
                    MemberSurvey newSurvey = MemberSurvey.createWithBasicSurvey(memberId, basicSurvey);
                    return surveyRepository.save(newSurvey);
                });
    }

    /**
     * 성향 테스트 제출 및 점수 계산
     */
    @Transactional
    public MemberSurvey submitAptitudeTest(Long memberId, AptitudeTest aptitudeTest) {
        MemberSurvey survey = surveyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SurveyNotFoundException(memberId));

        if (!survey.hasBasicSurvey()) {
            throw new BasicSurveyRequiredException(memberId);
        }

        // PUBLISHED 상태의 성향 테스트 문항 세트 조회
        SurveyQuestionSet questionSet = questionSetRepository
                .findPublishedByTypeWithQuestions(QuestionSetType.APTITUDE)
                .orElseThrow(() -> new SurveyQuestionSetNotFoundException("APTITUDE"));

        // 동적 검증: 발행된 문항 세트 기반으로 응답 수 검증
        validateAptitudeTestAnswers(aptitudeTest, questionSet);

        // 점수 계산
        SurveyResults results = resultCalculator.calculate(aptitudeTest, questionSet);

        // 결과 저장
        survey.completeAptitudeTest(aptitudeTest, results, questionSet.getVersion(), LocalDateTime.now());

        return surveyRepository.save(survey);
    }

    private void validateAptitudeTestAnswers(AptitudeTest test, SurveyQuestionSet questionSet) {
        // Part별 문항 수 계산
        Map<QuestionPart, Long> questionCounts = questionSet.getQuestions().stream()
                .filter(q -> q.getPart() != null)
                .collect(Collectors.groupingBy(SurveyQuestion::getPart, Collectors.counting()));

        List<String> errors = new ArrayList<>();

        int expectedPart1 = questionCounts.getOrDefault(QuestionPart.PART1, 0L).intValue();
        int expectedPart2 = questionCounts.getOrDefault(QuestionPart.PART2, 0L).intValue();
        int expectedPart3 = questionCounts.getOrDefault(QuestionPart.PART3, 0L).intValue();

        if (test.getPart1Answers().size() != expectedPart1) {
            errors.add("Part 1은 " + expectedPart1 + "문항 모두 응답해야 합니다");
        }
        if (test.getPart2Answers().size() != expectedPart2) {
            errors.add("Part 2는 " + expectedPart2 + "문항 모두 응답해야 합니다");
        }
        if (test.getPart3Answers().size() != expectedPart3) {
            errors.add("Part 3은 " + expectedPart3 + "문항 모두 응답해야 합니다");
        }

        if (!errors.isEmpty()) {
            throw new InvalidAptitudeTestAnswersException(String.join(", ", errors));
        }
    }

    public MemberSurvey getSurveyByMemberId(Long memberId) {
        return surveyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SurveyNotFoundException(memberId));
    }

    public Optional<MemberSurvey> findSurveyByMemberId(Long memberId) {
        return surveyRepository.findByMemberId(memberId);
    }

    public boolean existsByMemberId(Long memberId) {
        return surveyRepository.existsByMemberId(memberId);
    }

    public Page<MemberSurvey> getAllSurveys(Pageable pageable) {
        return surveyRepository.findAll(pageable);
    }

    /**
     * 설문 결과만 조회 (추천 직무)
     */
    public SurveyResults getResultsByMemberId(Long memberId) {
        MemberSurvey survey = getSurveyByMemberId(memberId);
        if (!survey.hasAptitudeTest()) {
            throw new AptitudeTestRequiredException(memberId);
        }
        return survey.getResults();
    }
}
