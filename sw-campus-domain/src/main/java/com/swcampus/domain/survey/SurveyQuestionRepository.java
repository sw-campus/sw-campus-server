package com.swcampus.domain.survey;

import java.util.List;
import java.util.Optional;

public interface SurveyQuestionRepository {

    SurveyQuestion save(SurveyQuestion question);

    Optional<SurveyQuestion> findById(Long questionId);

    Optional<SurveyQuestion> findByIdWithOptions(Long questionId);

    void delete(SurveyQuestion question);

    int findMaxOrderByQuestionSetId(Long questionSetId);

    List<SurveyQuestion> findAllByQuestionSetIdOrderByQuestionOrder(Long questionSetId);
}
