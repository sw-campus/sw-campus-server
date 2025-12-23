package com.swcampus.domain.organization;

import com.swcampus.domain.common.ApprovalStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationRepository {
    Organization save(Organization organization);

    Optional<Organization> findById(Long id);

    Optional<Organization> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Organization> findAll();

    List<Organization> findByNameContaining(String keyword);

    // 회원이 등록된 기관만 검색 (목록용)
    Page<Organization> searchByStatusAndKeyword(ApprovalStatus status, String keyword, Pageable pageable);

    java.util.List<Organization> findAllByIds(java.util.List<Long> ids);

    // 회원이 등록된 기관만 카운트 (통계용)
    long countAll();
    long countByApprovalStatus(ApprovalStatus status);
}
