package com.swcampus.infra.oauth;

import com.swcampus.domain.oauth.OAuthClient;
import com.swcampus.domain.oauth.OAuthProvider;
import com.swcampus.domain.oauth.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GitHubOAuthClient implements OAuthClient {

    private final RestTemplate restTemplate;

    @Value("${oauth.github.client-id}")
    private String clientId;

    @Value("${oauth.github.client-secret}")
    private String clientSecret;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String code) {
        // URL 인코딩된 code 디코딩 처리
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        
        String tokenUrl = "https://github.com/login/oauth/access_token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", decodedCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        String userInfoUrl = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            userInfoUrl, HttpMethod.GET, request, Map.class);

        Map<String, Object> body = response.getBody();

        String email = fetchEmail(accessToken);
        String name = (String) body.get("name");
        String login = (String) body.get("login");  // username, 항상 존재

        return OAuthUserInfo.builder()
            .provider(OAuthProvider.GITHUB)
            .providerId(String.valueOf(body.get("id")))
            .email(email)
            .name(name != null && !name.isBlank() ? name : login)  // name 없으면 username 사용
            .build();
    }

    @SuppressWarnings("unchecked")
    private String fetchEmail(String accessToken) {
        String emailUrl = "https://api.github.com/user/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
            emailUrl, HttpMethod.GET, request, List.class);

        List<Map<String, Object>> emails = response.getBody();
        if (emails == null) {
            return null;
        }

        return emails.stream()
            .filter(e -> Boolean.TRUE.equals(e.get("primary")))
            .map(e -> (String) e.get("email"))
            .findFirst()
            .orElse(null);
    }
}
