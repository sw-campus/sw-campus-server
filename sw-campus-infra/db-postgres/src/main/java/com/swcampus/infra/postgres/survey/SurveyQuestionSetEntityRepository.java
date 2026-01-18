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
    private final SurveyQuestionJpaRepository questionJpaRepository;
    private final SurveyOptionJpaRepository optionJpaRepository;

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
    public Optional<SurveyQuestionSet> findByTypeAndVersionWithQuestions(QuestionSetType type, int version) {
        return jpaRepository.findByTypeAndVersionWithQuestions(type, version)
                .map(SurveyQuestionSetEntity::toDomain);
    }

    @Override
    public List<SurveyQuestionSet> findAll() {
        return jpaRepository.findAll().stream()
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
            Long questionSetId = questionSet.getQuestionSetId();

            // 1. 먼저 해당 QuestionSet의 모든 Question ID 조회
            List<Long> questionIds = questionJpaRepository.findQuestionIdsByQuestionSetId(questionSetId);

            // 2. Option 삭제 (Question이 있는 경우에만)
            if (!questionIds.isEmpty()) {
                optionJpaRepository.deleteByQuestionIdIn(questionIds);
            }

            // 3. Question 삭제
            questionJpaRepository.deleteByQuestionSetId(questionSetId);

            // 4. QuestionSet 삭제 (JPQL로 직접 삭제하여 cascade 문제 회피)
            jpaRepository.deleteByIdDirectly(questionSetId);
        }
    }

    @Override
    public void archivePublishedByType(QuestionSetType type) {
        jpaRepository.archivePublishedByType(type);
    }
}
