package com.swcampus.infra.postgres.survey;

import com.swcampus.domain.survey.MemberSurvey;
import com.swcampus.domain.survey.MemberSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberSurveyEntityRepository implements MemberSurveyRepository {

    private final MemberSurveyJpaRepository jpaRepository;

    @Override
    public MemberSurvey save(MemberSurvey survey) {
        MemberSurveyEntity entity = jpaRepository.findById(survey.getUserId())
                .map(existing -> {
                    existing.update(survey);
                    return existing;
                })
                .orElseGet(() -> MemberSurveyEntity.from(survey));
        
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<MemberSurvey> findByUserId(Long userId) {
        return jpaRepository.findById(userId)
                .map(MemberSurveyEntity::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsById(userId);
    }

    @Override
    public Page<MemberSurvey> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(MemberSurveyEntity::toDomain);
    }
}
