package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.SurveyQuestion;
import com.swcampus.domain.survey.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyQuestionEntityRepository implements SurveyQuestionRepository {

    private final SurveyQuestionJpaRepository jpaRepository;
    private final SurveyOptionJpaRepository optionJpaRepository;

    @Override
    public SurveyQuestion save(SurveyQuestion question) {
        SurveyQuestionEntity entity;
        if (question.getQuestionId() != null) {
            entity = jpaRepository.findById(question.getQuestionId())
                    .orElse(SurveyQuestionEntity.from(question, question.getQuestionSetId()));
            entity.update(question);
        } else {
            entity = SurveyQuestionEntity.from(question, question.getQuestionSetId());
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<SurveyQuestion> findById(Long questionId) {
        return jpaRepository.findById(questionId)
                .map(SurveyQuestionEntity::toDomain);
    }

    @Override
    public Optional<SurveyQuestion> findByIdWithOptions(Long questionId) {
        return jpaRepository.findByIdWithOptions(questionId)
                .map(SurveyQuestionEntity::toDomain);
    }

    @Override
    public void delete(SurveyQuestion question) {
        if (question.getQuestionId() != null) {
            Long questionId = question.getQuestionId();
            // 1. Options 먼저 삭제 (FK 제약조건)
            optionJpaRepository.deleteByQuestionId(questionId);
            // 2. Question 삭제 (JPQL로 직접 삭제하여 cascade 문제 회피)
            jpaRepository.deleteByIdDirectly(questionId);
        }
    }

    @Override
    public int findMaxOrderByQuestionSetId(Long questionSetId) {
        return jpaRepository.findMaxOrderByQuestionSetId(questionSetId);
    }

    @Override
    public List<SurveyQuestion> findAllByQuestionSetIdOrderByQuestionOrder(Long questionSetId) {
        return jpaRepository.findAllByQuestionSetIdOrderByQuestionOrder(questionSetId)
                .stream()
                .map(SurveyQuestionEntity::toDomain)
                .toList();
    }
}
