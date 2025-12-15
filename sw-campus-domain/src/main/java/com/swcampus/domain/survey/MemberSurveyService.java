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
            Long memberId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        if (surveyRepository.existsByMemberId(memberId)) {
            throw new SurveyAlreadyExistsException(memberId);
        }

        MemberSurvey survey = MemberSurvey.create(
                memberId, major, bootcampCompleted,
                wantedJobs, licenses, hasGovCard, affordableAmount
        );

        return surveyRepository.save(survey);
    }

    public MemberSurvey getSurveyByMemberId(Long memberId) {
        return surveyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SurveyNotFoundException(memberId));
    }

    public java.util.Optional<MemberSurvey> findSurveyByMemberId(Long memberId) {
        return surveyRepository.findByMemberId(memberId);
    }

    public boolean existsByMemberId(Long memberId) {
        return surveyRepository.existsByMemberId(memberId);
    }

    @Transactional
    public MemberSurvey updateSurvey(
            Long memberId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        MemberSurvey survey = surveyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new SurveyNotFoundException(memberId));

        return updateSurveyInternal(survey, major, bootcampCompleted, wantedJobs, licenses, hasGovCard, affordableAmount);
    }

    @Transactional
    public MemberSurvey upsertSurvey(
            Long memberId,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        return surveyRepository.findByMemberId(memberId)
                .map(survey -> updateSurveyInternal(survey, major, bootcampCompleted, wantedJobs, licenses, hasGovCard, affordableAmount))
                .orElseGet(() -> {
                    MemberSurvey newSurvey = MemberSurvey.create(
                            memberId, major, bootcampCompleted, wantedJobs, licenses, hasGovCard, affordableAmount
                    );
                    return surveyRepository.save(newSurvey);
                });
    }

    private MemberSurvey updateSurveyInternal(
            MemberSurvey survey,
            String major,
            Boolean bootcampCompleted,
            String wantedJobs,
            String licenses,
            Boolean hasGovCard,
            BigDecimal affordableAmount
    ) {
        survey.update(major, bootcampCompleted, wantedJobs, licenses, hasGovCard, affordableAmount);
        return surveyRepository.save(survey);
    }

    public Page<MemberSurvey> getAllSurveys(Pageable pageable) {
        return surveyRepository.findAll(pageable);
    }
}
