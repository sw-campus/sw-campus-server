package com.swcampus.domain.organization;

import com.swcampus.domain.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public Organization getOrganizationByUserId(Long userId) {
        return organizationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자의 업체를 찾을 수 없습니다."));
    }
}
