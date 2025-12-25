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
        MemberSurveyEntity entity = MemberSurveyEntity.from(survey);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<MemberSurvey> findByMemberId(Long memberId) {
        return jpaRepository.findById(memberId)
                .map(MemberSurveyEntity::toDomain);
    }

    @Override
    public boolean existsByMemberId(Long memberId) {
        return jpaRepository.existsById(memberId);
    }

    @Override
    public Page<MemberSurvey> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(MemberSurveyEntity::toDomain);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        jpaRepository.deleteById(memberId);
    }
}
