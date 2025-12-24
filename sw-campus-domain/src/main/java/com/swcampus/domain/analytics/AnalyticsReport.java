package com.swcampus.domain.analytics;

import java.time.LocalDate;
import java.util.List;

/**
 * Google Analytics 통계 데이터를 나타내는 도메인 모델
 */
public class AnalyticsReport {
    
    private final long totalUsers;
    private final long activeUsers;
    private final long pageViews;
    private final long sessions;
    private final List<DailyStats> dailyStats;
    
    public AnalyticsReport(long totalUsers, long activeUsers, long pageViews, long sessions, List<DailyStats> dailyStats) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.pageViews = pageViews;
        this.sessions = sessions;
        this.dailyStats = dailyStats;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }
    
    public long getPageViews() {
        return pageViews;
    }
    
    public long getSessions() {
        return sessions;
    }
    
    public List<DailyStats> getDailyStats() {
        return dailyStats;
    }
    
    /**
     * 일별 통계 데이터
     */
    public record DailyStats(
        LocalDate date,
        long activeUsers,
        long pageViews
    ) {}
}
