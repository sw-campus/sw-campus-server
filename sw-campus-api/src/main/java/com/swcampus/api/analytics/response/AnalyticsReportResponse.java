package com.swcampus.api.analytics.response;

import com.swcampus.domain.analytics.AnalyticsReport;

import java.time.LocalDate;
import java.util.List;

/**
 * Google Analytics 통계 응답 DTO
 */
public record AnalyticsReportResponse(
    long totalUsers,
    long activeUsers,
    long newUsers,
    double averageEngagementTime,
    long pageViews,
    long sessions,
    List<DailyStatsResponse> dailyStats,
    List<DeviceStatResponse> deviceStats
) {
    
    public static AnalyticsReportResponse from(AnalyticsReport report) {
        List<DailyStatsResponse> dailyStatsResponses = report.getDailyStats().stream()
            .map(DailyStatsResponse::from)
            .toList();

        List<DeviceStatResponse> deviceStatsResponses = report.getDeviceStats().stream()
            .map(DeviceStatResponse::from)
            .toList();
        
        return new AnalyticsReportResponse(
            report.getTotalUsers(),
            report.getActiveUsers(),
            report.getNewUsers(),
            report.getAverageEngagementTime(),
            report.getPageViews(),
            report.getSessions(),
            dailyStatsResponses,
            deviceStatsResponses
        );
    }
    
    public record DailyStatsResponse(
        LocalDate date,
        long totalUsers,
        long newUsers,
        long pageViews
    ) {
        public static DailyStatsResponse from(AnalyticsReport.DailyStats stats) {
            return new DailyStatsResponse(stats.date(), stats.totalUsers(), stats.newUsers(), stats.pageViews());
        }
    }

    public record DeviceStatResponse(
        String category,
        long activeUsers
    ) {
        public static DeviceStatResponse from(AnalyticsReport.DeviceStat stats) {
            return new DeviceStatResponse(stats.category(), stats.activeUsers());
        }
    }
}
