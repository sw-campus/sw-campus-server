package com.swcampus.domain.auth;

import com.swcampus.domain.member.Role;

/**
 * 인증된 사용자 정보를 담는 Principal 클래스.
 * Spring Security 표준에 따라 Authentication.getPrincipal()을 통해 접근합니다.
 *
 * @param memberId 회원 ID
 * @param email 이메일
 * @param role 역할 (USER, ORGANIZATION, ADMIN)
 */
public record MemberPrincipal(
        Long memberId,
        String email,
        Role role
) {
}
