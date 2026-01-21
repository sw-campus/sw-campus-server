package com.swcampus.domain.survey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberSurveyRepository {
    MemberSurvey save(MemberSurvey survey);

    Optional<MemberSurvey> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    Page<MemberSurvey> findAll(Pageable pageable);

    void deleteByMemberId(Long memberId);

    /**
     * 성향 테스트 완료된 회원 수
     */
    long countByAptitudeTestNotNull();

    /**
     * 기초 설문만 완료된 회원 수
     */
    long countByBasicSurveyNotNullAndAptitudeTestNull();
}
