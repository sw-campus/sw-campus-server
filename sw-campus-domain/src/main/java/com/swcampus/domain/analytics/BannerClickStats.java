package com.swcampus.domain.analytics;

/**
 * 배너별 클릭 통계
 */
public record BannerClickStats(
    String bannerId,
    String bannerName,
    String bannerType,
    long clickCount
) {}
