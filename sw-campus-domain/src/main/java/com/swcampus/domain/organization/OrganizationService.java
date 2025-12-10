package com.swcampus.domain.organization;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.organization.dto.OrganizationSearchCondition;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public Organization getOrganizationByUserId(Long userId) {
        return organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자의 업체를 찾을 수 없습니다."));
    }

    public Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 업체를 찾을 수 없습니다."));
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public List<Organization> searchOrganizations(OrganizationSearchCondition condition) {
        return organizationRepository.searchOrganizations(condition);
    }
}
