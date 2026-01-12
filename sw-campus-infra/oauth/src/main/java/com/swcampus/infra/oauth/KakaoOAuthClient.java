package com.swcampus.infra.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.swcampus.domain.oauth.OAuthClient;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.OAuthUserInfo;
import com.swcampus.domain.oauth.exception.OAuthAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String REDIRECT_URI_KEY = "redirect_uri";
    private static final String CODE_KEY = "code";

    private final RestTemplate restTemplate;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String code) {
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(GRANT_TYPE_KEY, GRANT_TYPE);
        params.add(CLIENT_ID_KEY, clientId);
        params.add(CLIENT_SECRET_KEY, clientSecret);
        params.add(REDIRECT_URI_KEY, redirectUri);
        params.add(CODE_KEY, decodedCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
                TOKEN_URL, request, KakaoTokenResponse.class);

            KakaoTokenResponse body = response.getBody();
            if (body == null || body.accessToken() == null) {
                throw new OAuthAuthenticationException();
            }
            return body.accessToken();
        } catch (RestClientException e) {
            throw new OAuthAuthenticationException(e);
        }
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, request, KakaoUserInfoResponse.class);

            KakaoUserInfoResponse body = response.getBody();
            if (body == null) {
                throw new OAuthAuthenticationException();
            }

            return OAuthUserInfo.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(body.id()))
                .email(body.getEmail())
                .name(body.getNickname())
                .build();
        } catch (RestClientException e) {
            throw new OAuthAuthenticationException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoTokenResponse(
        @JsonProperty("access_token") String accessToken
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoUserInfoResponse(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record KakaoAccount(String email, Profile profile) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Profile(String nickname) {}

        public String getEmail() {
            return kakaoAccount != null ? kakaoAccount.email() : null;
        }

        public String getNickname() {
            return (kakaoAccount != null && kakaoAccount.profile() != null)
                ? kakaoAccount.profile().nickname() : null;
        }
    }
}
