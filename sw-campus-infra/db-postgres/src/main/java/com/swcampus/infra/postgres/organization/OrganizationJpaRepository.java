package com.swcampus.infra.postgres.organization;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.swcampus.domain.common.ApprovalStatus;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<OrganizationEntity> findByNameContaining(String name);

    // 재직증명서 제출한 기관만 목록 조회 (certificateKey가 있는 경우만)
    @Query("SELECT o FROM OrganizationEntity o WHERE o.certificateKey IS NOT NULL AND LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<OrganizationEntity> findWithCertificateByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.certificateKey IS NOT NULL AND o.approvalStatus = :status")
    Page<OrganizationEntity> findWithCertificateByApprovalStatus(ApprovalStatus status, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.certificateKey IS NOT NULL AND o.approvalStatus = :status AND LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<OrganizationEntity> findWithCertificateByApprovalStatusAndNameContainingIgnoreCase(ApprovalStatus status, String name, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.certificateKey IS NOT NULL")
    Page<OrganizationEntity> findAllWithCertificate(Pageable pageable);

    // 재직증명서 제출한 기관만 카운트 (certificateKey가 있는 경우만)
    @Query("SELECT COUNT(o) FROM OrganizationEntity o WHERE o.certificateKey IS NOT NULL")
    long countWithCertificate();

    @Query("SELECT COUNT(o) FROM OrganizationEntity o WHERE o.approvalStatus = :status AND o.certificateKey IS NOT NULL")
    long countByApprovalStatusWithCertificate(ApprovalStatus status);
}
