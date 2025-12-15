package com.swcampus.domain.organization;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.common.ResourceNotFoundException;
import com.swcampus.domain.organization.dto.UpdateOrganizationParams;
import com.swcampus.domain.organization.dto.UpdateOrganizationParams.FileUploadData;
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
    public Organization updateOrganization(Long orgId, Long userId, UpdateOrganizationParams params) {
        Organization organization = getOrganization(orgId);
        
        if (!organization.getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 업체를 수정할 권한이 없습니다.");
        }
        
        // 기본 정보 업데이트
        organization.updateInfo(
            params.name(), 
            params.description() != null ? params.description() : organization.getDescription()
        );
        
        // 홈페이지 업데이트
        if (params.homepage() != null) {
            organization.updateHomepage(params.homepage());
        }
        
        // 정부 인증 정보 업데이트
        if (params.govAuth() != null) {
            organization.setGovAuth(params.govAuth());
        }
        
        // 사업자등록증 업데이트
        String certificateUrl = uploadFileIfPresent(params.certificate(), "certificates");
        if (certificateUrl != null) {
            organization.updateCertificateUrl(certificateUrl);
        }
        
        // 로고 업데이트
        String logoUrl = uploadFileIfPresent(params.logo(), "logos");
        if (logoUrl != null) {
            organization.updateLogoUrl(logoUrl);
        }
        
        // 시설 이미지 업데이트
        String facilityUrl1 = uploadFileIfPresent(params.facilityImage1(), "facilities");
        String facilityUrl2 = uploadFileIfPresent(params.facilityImage2(), "facilities");
        String facilityUrl3 = uploadFileIfPresent(params.facilityImage3(), "facilities");
        String facilityUrl4 = uploadFileIfPresent(params.facilityImage4(), "facilities");
        
        // 시설 이미지는 null이 아닌 값만 업데이트
        organization.updateFacilityImages(
            facilityUrl1 != null ? facilityUrl1 : organization.getFacilityImageUrl(),
            facilityUrl2 != null ? facilityUrl2 : organization.getFacilityImageUrl2(),
            facilityUrl3 != null ? facilityUrl3 : organization.getFacilityImageUrl3(),
            facilityUrl4 != null ? facilityUrl4 : organization.getFacilityImageUrl4()
        );
        
        return organizationRepository.save(organization);
    }

    /**
     * @deprecated 이전 버전과의 호환성을 위해 유지. 새로운 코드에서는 UpdateOrganizationParams를 사용하세요.
     */
    @Deprecated
    @Transactional
    public Organization updateOrganization(Long orgId, Long userId, String name, String description, 
                                           byte[] fileContent, String fileName, String contentType) {
        FileUploadData certificate = null;
        if (fileContent != null && fileContent.length > 0) {
            certificate = new FileUploadData(fileContent, fileName, contentType);
        }
        
        return updateOrganization(orgId, userId, new UpdateOrganizationParams(
            name, description, null, null, certificate, null, null, null, null, null
        ));
    }

    private String uploadFileIfPresent(FileUploadData file, String folder) {
        if (file == null || file.content() == null || file.content().length == 0) {
            return null;
        }
        return fileStorageService.upload(file.content(), folder, file.fileName(), file.contentType());
    }
}
