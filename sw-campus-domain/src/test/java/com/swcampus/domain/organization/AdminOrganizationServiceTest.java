package com.swcampus.domain.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.swcampus.domain.common.ApprovalStatus;
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

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.exception.OrganizationNotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminOrganizationServiceTest {

    @InjectMocks
    private AdminOrganizationService adminOrganizationService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("기관 목록 조회/검색 성공")
    void searchOrganizations_Success() {
        // given
        ApprovalStatus status = ApprovalStatus.PENDING;
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 10);
        given(organizationRepository.searchByStatusAndKeyword(status, keyword, pageable)).willReturn(Page.empty());

        // when
        Page<Organization> result = adminOrganizationService.searchOrganizations(status, keyword, pageable);

        // then
        assertThat(result).isNotNull();
        verify(organizationRepository).searchByStatusAndKeyword(status, keyword, pageable);
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
    @DisplayName("기관 승인 성공")
    void approveOrganization_Success() {
        // given
        Long orgId = 1L;
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        Member member = Member.createOrganization("test@test.com", "password", "Test User", "nickname", "010-1234-5678", "Seoul");

        given(organizationRepository.findById(orgId)).willReturn(Optional.of(organization));
        given(memberRepository.findByOrgId(orgId)).willReturn(Optional.of(member));
        given(organizationRepository.save(any(Organization.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ApproveOrganizationResult result = adminOrganizationService.approveOrganization(orgId);

        // then
        assertThat(result.getOrganization().getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(result.getMemberEmail()).isEqualTo("test@test.com");
        verify(organizationRepository).save(organization);
        verify(memberRepository).findByOrgId(orgId);
    }

    @Test
    @DisplayName("기관 반려 성공 - Member 삭제 및 관리자 연락처 반환")
    void rejectOrganization_Success() {
        // given
        Long orgId = 1L;
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        Member member = Member.createOrganization("test@test.com", "password", "Test User", "nickname", "010-1234-5678", "Seoul");
        Member admin = Member.createUser("admin@test.com", "password", "Admin", "admin", "010-9999-9999", "Seoul");

        given(organizationRepository.findById(orgId)).willReturn(Optional.of(organization));
        given(memberRepository.findByOrgId(orgId)).willReturn(Optional.of(member));
        given(memberRepository.findFirstByRole(Role.ADMIN)).willReturn(Optional.of(admin));

        // when
        RejectOrganizationResult result = adminOrganizationService.rejectOrganization(orgId);

        // then
        assertThat(result.getMemberEmail()).isEqualTo("test@test.com");
        assertThat(result.getAdminEmail()).isEqualTo("admin@test.com");
        assertThat(result.getAdminPhone()).isEqualTo("010-9999-9999");
        verify(memberRepository).deleteById(member.getId());
    }
}
