package com.swcampus.domain.auth;

public enum TokenValidationResult {
    VALID,           // 유효한 토큰
    EXPIRED,         // 만료된 토큰 (A002)
    INVALID          // 위변조/잘못된 형식 (A001)
}
