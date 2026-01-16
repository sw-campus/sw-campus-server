package com.swcampus.infra.postgres.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyQuestionJpaRepository extends JpaRepository<SurveyQuestionEntity, Long> {

    @Query("SELECT q FROM SurveyQuestionEntity q LEFT JOIN FETCH q.options WHERE q.questionId = :id")
    Optional<SurveyQuestionEntity> findByIdWithOptions(@Param("id") Long id);

    @Query("SELECT COALESCE(MAX(q.questionOrder), 0) FROM SurveyQuestionEntity q WHERE q.questionSetId = :questionSetId")
    int findMaxOrderByQuestionSetId(@Param("questionSetId") Long questionSetId);

    @Query("SELECT q.questionId FROM SurveyQuestionEntity q WHERE q.questionSetId = :questionSetId")
    List<Long> findQuestionIdsByQuestionSetId(@Param("questionSetId") Long questionSetId);

    List<SurveyQuestionEntity> findAllByQuestionSetIdOrderByQuestionOrder(Long questionSetId);

    @Modifying
    @Query("DELETE FROM SurveyQuestionEntity q WHERE q.questionSetId = :questionSetId")
    void deleteByQuestionSetId(@Param("questionSetId") Long questionSetId);

    @Modifying
    @Query("DELETE FROM SurveyQuestionEntity q WHERE q.questionId = :id")
    void deleteByIdDirectly(@Param("id") Long id);
}
