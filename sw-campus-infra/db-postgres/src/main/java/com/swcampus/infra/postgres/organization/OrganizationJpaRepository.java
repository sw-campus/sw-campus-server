package com.swcampus.infra.postgres.organization;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.swcampus.domain.organization.ApprovalStatus;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<OrganizationEntity> findByNameContaining(String name);

    Page<OrganizationEntity> findByApprovalStatus(ApprovalStatus status, Pageable pageable);
}
