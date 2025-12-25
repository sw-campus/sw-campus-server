package com.swcampus.api.analytics.response;

import java.util.List;

import com.swcampus.domain.analytics.BannerClickStats;

public record BannerClickStatsResponse(
    String bannerId,
    String bannerName,
    String bannerType,
    long clickCount
) {
    public static BannerClickStatsResponse from(BannerClickStats stats) {
        return new BannerClickStatsResponse(
            stats.bannerId(),
            stats.bannerName(),
            stats.bannerType(),
            stats.clickCount()
        );
    }

    public static List<BannerClickStatsResponse> fromList(List<BannerClickStats> statsList) {
        return statsList.stream().map(BannerClickStatsResponse::from).toList();
    }
}
