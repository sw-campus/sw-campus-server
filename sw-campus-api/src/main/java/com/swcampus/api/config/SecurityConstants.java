package com.swcampus.api.config;

/**
 * Security 관련 공통 상수
 * Health check 경로 등 SecurityConfig와 JwtAuthenticationFilter에서 공유하는 경로 패턴을 정의
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // 인스턴스화 방지
    }

    /**
     * Health check 엔드포인트 경로 패턴
     * SecurityConfig의 permitAll()과 JwtAuthenticationFilter의 shouldNotFilter()에서 동일하게 사용
     */
    public static final String[] HEALTH_CHECK_PATTERNS = {
        "/actuator/health",
        "/actuator/health/**",
        "/api/actuator/health",
        "/api/actuator/health/**"
    };
}

