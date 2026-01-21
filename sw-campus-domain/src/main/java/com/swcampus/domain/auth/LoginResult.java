package com.swcampus.domain.auth;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.organization.Organization;
import lombok.Getter;

@Getter
public class LoginResult {
    private final String accessToken;
    private final String refreshToken;
    private final Member member;
    private final Organization organization;

    public LoginResult(String accessToken, String refreshToken, Member member) {
        this(accessToken, refreshToken, member, null);
    }

    public LoginResult(String accessToken, String refreshToken, Member member, Organization organization) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.member = member;
        this.organization = organization;
    }
}
