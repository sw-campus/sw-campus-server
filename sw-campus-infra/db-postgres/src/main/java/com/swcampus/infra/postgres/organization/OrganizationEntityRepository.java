package com.swcampus.infra.postgres.organization;

import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrganizationEntityRepository implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;

    @Override
    public Organization save(Organization organization) {
        OrganizationEntity entity = OrganizationEntity.from(organization);
        OrganizationEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Organization> findById(Long id) {
        return jpaRepository.findById(id)
                .map(OrganizationEntity::toDomain);
    }

    @Override
    public Optional<Organization> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(OrganizationEntity::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }
}
