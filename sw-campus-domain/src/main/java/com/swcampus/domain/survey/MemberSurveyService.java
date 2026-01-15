package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.AptitudeTestRequiredException;
import com.swcampus.domain.survey.exception.BasicSurveyRequiredException;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import com.swcampus.domain.survey.exception.SurveyQuestionSetNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        // 점수 계산
        SurveyResults results = resultCalculator.calculate(aptitudeTest, questionSet);

        // 결과 저장
        survey.completeAptitudeTest(aptitudeTest, results, questionSet.getVersion());

        return surveyRepository.save(survey);
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
