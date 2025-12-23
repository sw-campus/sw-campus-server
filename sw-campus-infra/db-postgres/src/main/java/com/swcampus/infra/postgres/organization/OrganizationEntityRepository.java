package com.swcampus.infra.postgres.organization;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.common.ApprovalStatus;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationRepository;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationEntityRepository implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;

    @Override
    @Transactional
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
    public List<Organization> findByNameContaining(String keyword) {
        return jpaRepository.findByNameContaining(keyword).stream()
                .map(OrganizationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Organization> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(OrganizationEntity::toDomain)
                .toList();
    }

    @Override
    public Page<Organization> searchByStatusAndKeyword(ApprovalStatus status, String keyword, Pageable pageable) {
        boolean hasStatus = status != null;
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        // 재직증명서 제출한 기관만 검색
        if (hasStatus && hasKeyword) {
            return jpaRepository.findWithCertificateByApprovalStatusAndNameContainingIgnoreCase(status, keyword, pageable)
                    .map(OrganizationEntity::toDomain);
        } else if (hasStatus) {
            return jpaRepository.findWithCertificateByApprovalStatus(status, pageable)
                    .map(OrganizationEntity::toDomain);
        } else if (hasKeyword) {
            return jpaRepository.findWithCertificateByNameContainingIgnoreCase(keyword, pageable)
                    .map(OrganizationEntity::toDomain);
        } else {
            return jpaRepository.findAllWithCertificate(pageable)
                    .map(OrganizationEntity::toDomain);
        }
    }

    @Override
    public long countAll() {
        return jpaRepository.countWithCertificate();
    }

    @Override
    public long countByApprovalStatus(ApprovalStatus status) {
        return jpaRepository.countByApprovalStatusWithCertificate(status);
    }
}
