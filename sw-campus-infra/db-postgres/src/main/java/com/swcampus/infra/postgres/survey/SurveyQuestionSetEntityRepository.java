package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.QuestionSetStatus;
import com.swcampus.domain.survey.QuestionSetType;
import com.swcampus.domain.survey.SurveyQuestionSet;
import com.swcampus.domain.survey.SurveyQuestionSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SurveyQuestionSetEntityRepository implements SurveyQuestionSetRepository {

    private final SurveyQuestionSetJpaRepository jpaRepository;

    @Override
    public SurveyQuestionSet save(SurveyQuestionSet questionSet) {
        SurveyQuestionSetEntity entity;
        if (questionSet.getQuestionSetId() != null) {
            entity = jpaRepository.findById(questionSet.getQuestionSetId())
                    .orElse(SurveyQuestionSetEntity.from(questionSet));
            entity.update(questionSet);
        } else {
            entity = SurveyQuestionSetEntity.from(questionSet);
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<SurveyQuestionSet> findById(Long questionSetId) {
        return jpaRepository.findById(questionSetId)
                .map(SurveyQuestionSetEntity::toDomainWithoutQuestions);
    }

    @Override
    public Optional<SurveyQuestionSet> findByIdWithQuestions(Long questionSetId) {
        return jpaRepository.findByIdWithQuestions(questionSetId)
                .map(SurveyQuestionSetEntity::toDomain);
    }

    @Override
    public Optional<SurveyQuestionSet> findPublishedByType(QuestionSetType type) {
        return jpaRepository.findByTypeAndStatus(type, QuestionSetStatus.PUBLISHED)
                .map(SurveyQuestionSetEntity::toDomainWithoutQuestions);
    }

    @Override
    public Optional<SurveyQuestionSet> findPublishedByTypeWithQuestions(QuestionSetType type) {
        return jpaRepository.findByTypeAndStatusWithQuestions(type, QuestionSetStatus.PUBLISHED)
                .map(SurveyQuestionSetEntity::toDomain);
    }

    @Override
    public List<SurveyQuestionSet> findAllByType(QuestionSetType type) {
        return jpaRepository.findAllByType(type).stream()
                .map(SurveyQuestionSetEntity::toDomainWithoutQuestions)
                .collect(Collectors.toList());
    }

    @Override
    public int findMaxVersionByType(QuestionSetType type) {
        return jpaRepository.findMaxVersionByType(type);
    }

    @Override
    public void delete(SurveyQuestionSet questionSet) {
        if (questionSet.getQuestionSetId() != null) {
            jpaRepository.deleteById(questionSet.getQuestionSetId());
        }
    }

    @Override
    public void archivePublishedByType(QuestionSetType type) {
        jpaRepository.archivePublishedByType(type);
    }
}
