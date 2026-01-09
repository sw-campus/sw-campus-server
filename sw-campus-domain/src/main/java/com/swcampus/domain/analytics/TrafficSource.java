package com.swcampus.domain.analytics;

/**
 * 트래픽 소스 통계 데이터
 * GA4의 sessionSource/sessionMedium 기반
 */
public record TrafficSource(
    String source,
    String medium,
    long sessions,
    long users
) {}
