package com.swcampus.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfo {
    private final String accessToken;
    private final String refreshToken;
}
