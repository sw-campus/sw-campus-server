package com.swcampus.infra.oauth;

import com.swcampus.domain.oauth.OAuthClient;
import com.swcampus.domain.oauth.OAuthClientFactory;
import com.swcampus.domain.oauth.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthClientFactoryImpl implements OAuthClientFactory {

    private final GoogleOAuthClient googleOAuthClient;
    private final GitHubOAuthClient gitHubOAuthClient;

    @Override
    public OAuthClient getClient(OAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> googleOAuthClient;
            case GITHUB -> gitHubOAuthClient;
        };
    }
}
