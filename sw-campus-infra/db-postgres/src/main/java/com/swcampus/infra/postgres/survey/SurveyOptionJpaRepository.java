package com.swcampus.infra.postgres.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyOptionJpaRepository extends JpaRepository<SurveyOptionEntity, Long> {

    @Query("SELECT COALESCE(MAX(o.optionOrder), 0) FROM SurveyOptionEntity o WHERE o.questionId = :questionId")
    int findMaxOrderByQuestionId(@Param("questionId") Long questionId);

    List<SurveyOptionEntity> findAllByQuestionIdOrderByOptionOrder(Long questionId);

    @Modifying
    @Query("DELETE FROM SurveyOptionEntity o WHERE o.questionId IN :questionIds")
    void deleteByQuestionIdIn(@Param("questionIds") List<Long> questionIds);

    @Modifying
    @Query("DELETE FROM SurveyOptionEntity o WHERE o.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);

    @Modifying
    @Query("DELETE FROM SurveyOptionEntity o WHERE o.optionId = :id")
    void deleteByIdDirectly(@Param("id") Long id);
}
