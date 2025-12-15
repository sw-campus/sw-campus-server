package com.swcampus.domain.survey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberSurveyRepository {
    MemberSurvey save(MemberSurvey survey);
    Optional<MemberSurvey> findByMemberId(Long memberId);
    boolean existsByMemberId(Long memberId);
    Page<MemberSurvey> findAll(Pageable pageable);
}
