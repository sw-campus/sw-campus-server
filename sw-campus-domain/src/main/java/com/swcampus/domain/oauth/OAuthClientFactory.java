package com.swcampus.domain.oauth;

/**
 * OAuth Provider별 클라이언트를 제공하는 팩토리 인터페이스
 */
public interface OAuthClientFactory {

    OAuthClient getClient(OAuthProvider provider);
}
