package com.swcampus.api.analytics.response;

import com.swcampus.domain.analytics.TrafficSource;

import java.util.List;

/**
 * 트래픽 소스 통계 응답 DTO
 */
public record TrafficSourceResponse(
    String source,
    String medium,
    long sessions,
    long users
) {
    public static TrafficSourceResponse from(TrafficSource domain) {
        return new TrafficSourceResponse(
            domain.source(),
            domain.medium(),
            domain.sessions(),
            domain.users()
        );
    }

    public static List<TrafficSourceResponse> fromList(List<TrafficSource> domains) {
        return domains.stream()
            .map(TrafficSourceResponse::from)
            .toList();
    }
}
