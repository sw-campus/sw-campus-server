package com.swcampus.domain.organization;

import java.util.List;
import java.util.Optional;

import com.swcampus.domain.organization.dto.OrganizationSearchCondition;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(Long id);
    Optional<Organization> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<Organization> findAll();
    
    List<Organization> searchOrganizations(OrganizationSearchCondition condition);
}
