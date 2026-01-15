package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.QuestionSetStatus;
import com.swcampus.domain.survey.QuestionSetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyQuestionSetJpaRepository extends JpaRepository<SurveyQuestionSetEntity, Long> {

    Optional<SurveyQuestionSetEntity> findByTypeAndStatus(QuestionSetType type, QuestionSetStatus status);

    @Query("SELECT qs FROM SurveyQuestionSetEntity qs LEFT JOIN FETCH qs.questions q LEFT JOIN FETCH q.options WHERE qs.questionSetId = :id")
    Optional<SurveyQuestionSetEntity> findByIdWithQuestions(@Param("id") Long id);

    @Query("SELECT qs FROM SurveyQuestionSetEntity qs LEFT JOIN FETCH qs.questions q LEFT JOIN FETCH q.options WHERE qs.type = :type AND qs.status = :status")
    Optional<SurveyQuestionSetEntity> findByTypeAndStatusWithQuestions(
            @Param("type") QuestionSetType type,
            @Param("status") QuestionSetStatus status
    );

    List<SurveyQuestionSetEntity> findAllByType(QuestionSetType type);

    @Query("SELECT COALESCE(MAX(qs.version), 0) FROM SurveyQuestionSetEntity qs WHERE qs.type = :type")
    int findMaxVersionByType(@Param("type") QuestionSetType type);

    @Modifying
    @Query("UPDATE SurveyQuestionSetEntity qs SET qs.status = 'ARCHIVED' WHERE qs.type = :type AND qs.status = 'PUBLISHED'")
    void archivePublishedByType(@Param("type") QuestionSetType type);
}
