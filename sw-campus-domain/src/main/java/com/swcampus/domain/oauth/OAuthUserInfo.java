package com.swcampus.domain.oauth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {
    private final OAuthProvider provider;
    private final String providerId;
    private final String email;
    private final String name;
}
