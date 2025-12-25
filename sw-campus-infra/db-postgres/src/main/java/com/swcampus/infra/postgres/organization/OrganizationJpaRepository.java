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

    List<OrganizationEntity> findByNameContainingIgnoreCase(String name);

    // 회원이 등록된 기관만 목록 조회 (Member.orgId가 존재하는 경우만)
    @Query("SELECT o FROM OrganizationEntity o WHERE EXISTS (SELECT 1 FROM MemberEntity m WHERE m.orgId = o.id) AND LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<OrganizationEntity> findWithMemberRegistrationByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o WHERE EXISTS (SELECT 1 FROM MemberEntity m WHERE m.orgId = o.id) AND o.approvalStatus = :status")
    Page<OrganizationEntity> findWithMemberRegistrationByApprovalStatus(ApprovalStatus status, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o WHERE EXISTS (SELECT 1 FROM MemberEntity m WHERE m.orgId = o.id) AND o.approvalStatus = :status AND LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<OrganizationEntity> findWithMemberRegistrationByApprovalStatusAndNameContainingIgnoreCase(ApprovalStatus status, String name, Pageable pageable);

    @Query("SELECT o FROM OrganizationEntity o WHERE EXISTS (SELECT 1 FROM MemberEntity m WHERE m.orgId = o.id)")
    Page<OrganizationEntity> findAllWithMemberRegistration(Pageable pageable);

    // 회원이 등록된 기관만 카운트 (Member.orgId가 존재하는 경우만)
    @Query("SELECT COUNT(o) FROM OrganizationEntity o WHERE EXISTS (SELECT 1 FROM MemberEntity m WHERE m.orgId = o.id)")
    long countWithMemberRegistration();

    @Query("SELECT COUNT(o) FROM OrganizationEntity o WHERE o.approvalStatus = :status AND EXISTS (SELECT 1 FROM MemberEntity m WHERE m.orgId = o.id)")
    long countByApprovalStatusWithMemberRegistration(ApprovalStatus status);
}
