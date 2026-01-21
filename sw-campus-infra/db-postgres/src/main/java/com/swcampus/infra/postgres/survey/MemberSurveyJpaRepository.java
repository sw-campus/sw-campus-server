package com.swcampus.infra.postgres.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberSurveyJpaRepository extends JpaRepository<MemberSurveyEntity, Long> {

    @Query("SELECT COUNT(e) FROM MemberSurveyEntity e WHERE e.aptitudeTest IS NOT NULL")
    long countByAptitudeTestNotNull();

    @Query("SELECT COUNT(e) FROM MemberSurveyEntity e WHERE e.basicSurvey IS NOT NULL AND e.aptitudeTest IS NULL")
    long countByBasicSurveyNotNullAndAptitudeTestNull();
}
