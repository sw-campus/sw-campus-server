package com.swcampus.domain.analytics;

import java.time.LocalDate;
import java.util.List;

/**
 * Google Analytics 통계 데이터를 나타내는 도메인 모델
 */
public class AnalyticsReport {
    
    private final long totalUsers;
    private final long activeUsers;
    private final long newUsers;
    private final double averageEngagementTime;
    private final long pageViews;
    private final long sessions;
    private final List<DailyStats> dailyStats;
    private final List<DeviceStat> deviceStats;

    public AnalyticsReport(long totalUsers, long activeUsers, long newUsers, double averageEngagementTime, long pageViews, long sessions, List<DailyStats> dailyStats, List<DeviceStat> deviceStats) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.newUsers = newUsers;
        this.averageEngagementTime = averageEngagementTime;
        this.pageViews = pageViews;
        this.sessions = sessions;
        this.dailyStats = dailyStats;
        this.deviceStats = deviceStats;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public long getActiveUsers() {
        return activeUsers;
    }

    public long getNewUsers() {
        return newUsers;
    }

    public double getAverageEngagementTime() {
        return averageEngagementTime;
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

    public List<DeviceStat> getDeviceStats() {
        return deviceStats;
    }
    
    /**
     * 일별 통계 데이터
     */
    public record DailyStats(
        LocalDate date,
        long totalUsers,
        long newUsers,
        long pageViews
    ) {}

    /**
     * 기기별 통계 데이터
     */
    public record DeviceStat(
        String category,
        long activeUsers
    ) {}
}
