package com.swcampus.domain.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.swcampus.domain.organization.exception.OrganizationNotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminOrganizationServiceTest {

    @InjectMocks
    private AdminOrganizationService adminOrganizationService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Test
    @DisplayName("상태별 기관 목록 조회 성공")
    void getOrganizationsByStatus_Success() {
        // given
        ApprovalStatus status = ApprovalStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);
        given(organizationRepository.findByApprovalStatus(status, pageable)).willReturn(Page.empty());

        // when
        Page<Organization> result = adminOrganizationService.getOrganizationsByStatus(status, pageable);

        // then
        assertThat(result).isNotNull();
        verify(organizationRepository).findByApprovalStatus(status, pageable);
    }

    @Test
    @DisplayName("기관 상세 조회 성공")
    void getOrganizationDetail_Success() {
        // given
        Long id = 1L;
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        given(organizationRepository.findById(id)).willReturn(Optional.of(organization));

        // when
        Organization result = adminOrganizationService.getOrganizationDetail(id);

        // then
        assertThat(result).isEqualTo(organization);
    }

    @Test
    @DisplayName("기관 상세 조회 실패 - 존재하지 않음")
    void getOrganizationDetail_NotFound() {
        // given
        Long id = 1L;
        given(organizationRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminOrganizationService.getOrganizationDetail(id))
                .isInstanceOf(OrganizationNotFoundException.class);
    }

    @Test
    @DisplayName("기관 승인 성공 - 이미 승인된 기관도 멱등성 유지")
    void approveOrganization_AlreadyApproved_IdempotentSuccess() {
        // given
        Long id = 1L;
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        organization.approve(); // 이미 승인된 상태
        given(organizationRepository.findById(id)).willReturn(Optional.of(organization));
        given(organizationRepository.save(any(Organization.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Organization result = adminOrganizationService.approveOrganization(id);

        // then
        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(organizationRepository).save(organization);
    }

    @Test
    @DisplayName("기관 승인 성공")
    void approveOrganization_Success() {
        // given
        Long id = 1L;
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        given(organizationRepository.findById(id)).willReturn(Optional.of(organization));
        given(organizationRepository.save(any(Organization.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Organization result = adminOrganizationService.approveOrganization(id);

        // then
        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(organizationRepository).save(organization);
    }

    @Test
    @DisplayName("기관 반려 성공")
    void rejectOrganization_Success() {
        // given
        Long id = 1L;
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        given(organizationRepository.findById(id)).willReturn(Optional.of(organization));
        given(organizationRepository.save(any(Organization.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Organization result = adminOrganizationService.rejectOrganization(id);

        // then
        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        verify(organizationRepository).save(organization);
    }
}
