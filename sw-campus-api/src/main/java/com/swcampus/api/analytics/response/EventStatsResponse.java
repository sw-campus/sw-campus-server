package com.swcampus.api.analytics.response;

import com.swcampus.domain.analytics.EventStats;

import java.util.List;

/**
 * 이벤트 통계 응답 DTO
 */
public record EventStatsResponse(
    long bannerClicks,
    long bigBannerClicks,
    long middleBannerClicks,
    long smallBannerClicks,
    long applyButtonClicks,
    long shareClicks,
    List<EventDetailResponse> events
) {
    
    public static EventStatsResponse from(EventStats stats) {
        List<EventDetailResponse> events = stats.getEventDetails().stream()
            .map(EventDetailResponse::from)
            .toList();
        
        return new EventStatsResponse(
            stats.getBannerClicks(),
            stats.getBigBannerClicks(),
            stats.getMiddleBannerClicks(),
            stats.getSmallBannerClicks(),
            stats.getApplyButtonClicks(),
            stats.getShareClicks(),
            events
        );
    }
    
    public record EventDetailResponse(
        String eventName,
        long eventCount
    ) {
        public static EventDetailResponse from(EventStats.EventDetail detail) {
            return new EventDetailResponse(detail.eventName(), detail.eventCount());
        }
    }
}
