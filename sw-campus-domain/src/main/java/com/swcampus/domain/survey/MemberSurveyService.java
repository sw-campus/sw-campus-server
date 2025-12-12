package com.swcampus.domain.survey;

import com.swcampus.domain.survey.exception.SurveyAlreadyExistsException;
import com.swcampus.domain.survey.exception.SurveyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSurveyService {

    private final MemberSurveyRepository surveyRepository;

    @Transactional
    public MemberSurvey createSurvey(
            Long userId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        if (surveyRepository.existsByUserId(userId)) {
            throw new SurveyAlreadyExistsException();
        }

        MemberSurvey survey = MemberSurvey.create(
                userId, major, bootcampCompleted,
                wantedJobs, licenses, hasGovCard, affordableAmount
        );

        return surveyRepository.save(survey);
    }

    public MemberSurvey getSurveyByUserId(Long userId) {
        return surveyRepository.findByUserId(userId)
                .orElseThrow(SurveyNotFoundException::new);
    }

    @Transactional
    public MemberSurvey updateSurvey(
            Long userId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        MemberSurvey survey = surveyRepository.findByUserId(userId)
                .orElseThrow(SurveyNotFoundException::new);

        survey.update(major, bootcampCompleted, wantedJobs,
                licenses, hasGovCard, affordableAmount);

        return surveyRepository.save(survey);
    }

    public Page<MemberSurvey> getAllSurveys(Pageable pageable) {
        return surveyRepository.findAll(pageable);
    }
}
