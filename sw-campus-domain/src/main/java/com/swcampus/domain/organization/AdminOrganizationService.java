package com.swcampus.domain.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.organization.exception.OrganizationNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrganizationService {

    private final OrganizationRepository organizationRepository;

    public Page<Organization> searchOrganizations(ApprovalStatus status, String keyword, Pageable pageable) {
        return organizationRepository.searchByStatusAndKeyword(status, keyword, pageable);
    }

    public Organization getOrganizationDetail(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    @Transactional
    public Organization approveOrganization(Long id) {
        Organization organization = getOrganizationDetail(id);
        organization.approve();
        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization rejectOrganization(Long id) {
        Organization organization = getOrganizationDetail(id);
        organization.reject();
        return organizationRepository.save(organization);
    }
}
