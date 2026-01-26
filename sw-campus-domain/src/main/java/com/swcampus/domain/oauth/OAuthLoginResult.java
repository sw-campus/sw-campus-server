package com.swcampus.domain.oauth;

import com.swcampus.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthLoginResult {
    private final String accessToken;
    private final String refreshToken;
    private final Member member;
    private final boolean isFirstLogin;
}
