package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.SurveyOption;
import com.swcampus.domain.survey.SurveyOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyOptionEntityRepository implements SurveyOptionRepository {

    private final SurveyOptionJpaRepository jpaRepository;

    @Override
    public SurveyOption save(SurveyOption option) {
        SurveyOptionEntity entity;
        if (option.getOptionId() != null) {
            entity = jpaRepository.findById(option.getOptionId())
                    .orElse(SurveyOptionEntity.from(option, option.getQuestionId()));
            entity.update(option);
        } else {
            entity = SurveyOptionEntity.from(option, option.getQuestionId());
        }
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<SurveyOption> findById(Long optionId) {
        return jpaRepository.findById(optionId)
                .map(SurveyOptionEntity::toDomain);
    }

    @Override
    public void delete(SurveyOption option) {
        if (option.getOptionId() != null) {
            // JPA 컬렉션 관리 문제를 피하기 위해 JPQL로 직접 삭제
            jpaRepository.deleteByIdDirectly(option.getOptionId());
        }
    }

    @Override
    public int findMaxOrderByQuestionId(Long questionId) {
        return jpaRepository.findMaxOrderByQuestionId(questionId);
    }

    @Override
    public List<SurveyOption> findAllByQuestionIdOrderByOptionOrder(Long questionId) {
        return jpaRepository.findAllByQuestionIdOrderByOptionOrder(questionId)
                .stream()
                .map(SurveyOptionEntity::toDomain)
                .toList();
    }
}
