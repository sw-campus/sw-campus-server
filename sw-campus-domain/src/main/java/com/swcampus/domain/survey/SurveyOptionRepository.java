package com.swcampus.domain.survey;

import java.util.List;
import java.util.Optional;

public interface SurveyOptionRepository {

    SurveyOption save(SurveyOption option);

    Optional<SurveyOption> findById(Long optionId);

    void delete(SurveyOption option);

    int findMaxOrderByQuestionId(Long questionId);

    List<SurveyOption> findAllByQuestionIdOrderByOptionOrder(Long questionId);
}
