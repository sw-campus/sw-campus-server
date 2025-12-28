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
     * 실제 사용 경로: /actuator/health/readiness, /actuator/health/liveness
     * Ant-style 패턴 매칭을 사용하여 SecurityConfig와 동일한 방식으로 매칭
     * "/actuator/health/**" 패턴이 "/actuator/health" 및 모든 하위 경로를 포함하므로 하나만 사용
     */
    public static final String[] HEALTH_CHECK_PATTERNS = {
        "/actuator/health/**"
    };
}

