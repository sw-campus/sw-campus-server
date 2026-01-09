package com.swcampus.api.analytics.response;

import com.swcampus.domain.analytics.TrafficSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 트래픽 소스 통계 응답 DTO
 */
public record TrafficSourceResponse(
    @Schema(description = "트래픽 소스", example = "google")
    String source,
    @Schema(description = "트래픽 매체", example = "cpc")
    String medium,
    @Schema(description = "세션 수", example = "120")
    long sessions,
    @Schema(description = "사용자 수", example = "100")
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
