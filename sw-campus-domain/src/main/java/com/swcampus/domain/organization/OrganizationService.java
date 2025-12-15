package com.swcampus.domain.organization;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.organization.dto.UpdateOrganizationParams;
import com.swcampus.domain.storage.FileStorageService;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final FileStorageService fileStorageService;

    public Organization getOrganizationByUserId(Long userId) {
        return organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자의 업체를 찾을 수 없습니다."));
    }

    public Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 업체를 찾을 수 없습니다."));
    }

    public List<Organization> getOrganizationList() {
        return organizationRepository.findAll();
    }

    public List<Organization> getOrganizationList(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return organizationRepository.findByNameContaining(keyword);
        }
        return organizationRepository.findAll();
    }

    @Transactional
    public Organization updateOrganization(Long orgId, Long userId, String name, String description, 
                                           byte[] fileContent, String fileName, String contentType) {
        Organization organization = getOrganization(orgId);
        
        if (!organization.getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 업체를 수정할 권한이 없습니다.");
        }
        
        String certificateUrl = organization.getCertificateUrl();
        if (fileContent != null && fileContent.length > 0) {
            certificateUrl = fileStorageService.upload(fileContent, "certificates", fileName, contentType);
        }
        
        organization.updateInfo(name, description);
        if (certificateUrl != null) {
            organization.updateCertificateUrl(certificateUrl);
        }
        
        return organizationRepository.save(organization);
    }
}
