package com.swcampus.domain.analytics;

import java.util.List;

/**
 * 이벤트 통계 데이터를 나타내는 도메인 모델
 */
public class EventStats {
    
    private final long bannerClicks;
    private final long bigBannerClicks;
    private final long middleBannerClicks;
    private final long smallBannerClicks;
    private final long applyButtonClicks;
    private final long shareClicks;
    private final List<EventDetail> eventDetails;
    
    public EventStats(
        long bannerClicks, 
        long bigBannerClicks,
        long middleBannerClicks,
        long smallBannerClicks,
        long applyButtonClicks, 
        long shareClicks, 
        List<EventDetail> eventDetails
    ) {
        this.bannerClicks = bannerClicks;
        this.bigBannerClicks = bigBannerClicks;
        this.middleBannerClicks = middleBannerClicks;
        this.smallBannerClicks = smallBannerClicks;
        this.applyButtonClicks = applyButtonClicks;
        this.shareClicks = shareClicks;
        this.eventDetails = eventDetails;
    }
    
    public long getBannerClicks() {
        return bannerClicks;
    }

    public long getBigBannerClicks() {
        return bigBannerClicks;
    }

    public long getMiddleBannerClicks() {
        return middleBannerClicks;
    }

    public long getSmallBannerClicks() {
        return smallBannerClicks;
    }
    
    public long getApplyButtonClicks() {
        return applyButtonClicks;
    }
    
    public long getShareClicks() {
        return shareClicks;
    }
    
    public List<EventDetail> getEventDetails() {
        return eventDetails;
    }
    
    /**
     * 개별 이벤트 상세 데이터
     */
    public record EventDetail(
        String eventName,
        long eventCount,
        String bannerId,
        String bannerType,
        String lectureId
    ) {}
}
