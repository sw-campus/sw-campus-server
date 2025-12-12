package com.swcampus.infra.postgres.survey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSurveyJpaRepository extends JpaRepository<MemberSurveyEntity, Long> {
}
