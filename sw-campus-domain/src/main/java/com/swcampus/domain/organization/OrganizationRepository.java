package com.swcampus.domain.organization;

import java.util.Optional;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(Long id);
    Optional<Organization> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
