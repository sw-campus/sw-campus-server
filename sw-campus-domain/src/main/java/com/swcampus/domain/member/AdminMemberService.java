package com.swcampus.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public Page<Member> searchMembers(Role role, String keyword, Pageable pageable) {
        return memberRepository.searchByRoleAndKeyword(role, keyword, pageable);
    }

    /**
     * 회원 역할별 통계를 조회합니다.
     */
    public MemberRoleStats getStats() {
        long total = memberRepository.countAll();
        long userCount = memberRepository.countByRole(Role.USER);
        long organizationCount = memberRepository.countByRole(Role.ORGANIZATION);
        long adminCount = memberRepository.countByRole(Role.ADMIN);
        return new MemberRoleStats(total, userCount, organizationCount, adminCount);
    }

    public record MemberRoleStats(long total, long user, long organization, long admin) {}
}
