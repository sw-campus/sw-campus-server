package com.swcampus.infra.postgres.organization;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;
import com.swcampus.domain.organization.dto.OrganizationSearchCondition;

import lombok.RequiredArgsConstructor;

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

    @Override
    public List<Organization> findAll() {
        return jpaRepository.findAll().stream()
                .map(OrganizationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Organization> searchOrganizations(OrganizationSearchCondition condition) {
        List<OrganizationEntity> entities;
        
        if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
            entities = jpaRepository.findByNameContaining(condition.getKeyword());
        } else {
            entities = jpaRepository.findAll();
        }

        return entities.stream()
                .map(OrganizationEntity::toDomain)
                .toList();
    }
}
