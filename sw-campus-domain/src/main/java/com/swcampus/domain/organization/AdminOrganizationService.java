package com.swcampus.domain.organization;

import com.swcampus.domain.common.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.member.exception.AdminNotFoundException;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.organization.exception.OrganizationNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;

    public Page<Organization> searchOrganizations(ApprovalStatus status, String keyword, Pageable pageable) {
        return organizationRepository.searchByStatusAndKeyword(status, keyword, pageable);
    }

    public Organization getOrganizationDetail(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    @Transactional
    public ApproveOrganizationResult approveOrganization(Long id) {
        Organization organization = getOrganizationDetail(id);

        // 해당 기관에 연결된 Member 조회
        Member member = memberRepository.findByOrgId(id)
                .orElseThrow(() -> new MemberNotFoundException("해당 기관에 연결된 회원이 없습니다: " + id));

        // Organization.userId를 새 사용자 ID로 매핑
        organization.setUserId(member.getId());
        organization.approve();

        Organization savedOrg = organizationRepository.save(organization);
        return new ApproveOrganizationResult(savedOrg, member.getEmail());
    }

    @Transactional
    public RejectOrganizationResult rejectOrganization(Long id) {
        // 기관 존재 여부 확인
        getOrganizationDetail(id);

        // 해당 기관에 연결된 Member 조회
        Member member = memberRepository.findByOrgId(id)
                .orElseThrow(() -> new MemberNotFoundException("해당 기관에 연결된 회원이 없습니다: " + id));

        // 반려 메일 발송을 위해 삭제 전 이메일 저장
        String memberEmail = member.getEmail();

        // 관리자 연락처 조회
        Member admin = memberRepository.findFirstByRole(Role.ADMIN)
                .orElseThrow(AdminNotFoundException::new);

        // Organization은 PENDING 상태 유지 (reject() 호출하지 않음)
        // Member만 삭제
        memberRepository.deleteById(member.getId());

        return new RejectOrganizationResult(memberEmail, admin.getEmail(), admin.getPhone());
    }

    /**
     * 기관 상태별 통계를 조회합니다.
     * 회원이 등록된(Member.orgId가 존재하는) 기관만 카운트합니다.
     */
    public OrganizationApprovalStats getStats() {
        long total = organizationRepository.countAll();
        long pending = organizationRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approved = organizationRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long rejected = organizationRepository.countByApprovalStatus(ApprovalStatus.REJECTED);
        return new OrganizationApprovalStats(total, pending, approved, rejected);
    }

    public record OrganizationApprovalStats(long total, long pending, long approved, long rejected) {}
}
