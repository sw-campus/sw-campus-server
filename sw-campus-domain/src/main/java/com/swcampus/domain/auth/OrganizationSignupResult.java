package com.swcampus.domain.auth;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.organization.Organization;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrganizationSignupResult {
    private final Member member;
    private final Organization organization;
}
