package com.swcampus.infra.postgres.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
