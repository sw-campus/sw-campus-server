package com.swcampus.domain.oauth;

public interface OAuthClient {
    OAuthUserInfo getUserInfo(String code);
}
