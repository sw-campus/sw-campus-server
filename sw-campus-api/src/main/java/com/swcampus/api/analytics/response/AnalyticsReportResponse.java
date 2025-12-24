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
    long pageViews,
    long sessions,
    List<DailyStatsResponse> dailyStats
) {
    
    public static AnalyticsReportResponse from(AnalyticsReport report) {
        List<DailyStatsResponse> dailyStatsResponses = report.getDailyStats().stream()
            .map(DailyStatsResponse::from)
            .toList();
        
        return new AnalyticsReportResponse(
            report.getTotalUsers(),
            report.getActiveUsers(),
            report.getPageViews(),
            report.getSessions(),
            dailyStatsResponses
        );
    }
    
    public record DailyStatsResponse(
        LocalDate date,
        long activeUsers,
        long pageViews
    ) {
        public static DailyStatsResponse from(AnalyticsReport.DailyStats stats) {
            return new DailyStatsResponse(stats.date(), stats.activeUsers(), stats.pageViews());
        }
    }
}
