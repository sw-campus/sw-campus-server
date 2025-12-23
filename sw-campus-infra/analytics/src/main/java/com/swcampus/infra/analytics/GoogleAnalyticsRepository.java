package com.swcampus.infra.analytics;

import com.google.analytics.data.v1beta.*;
import com.swcampus.domain.analytics.AnalyticsReport;
import com.swcampus.domain.analytics.AnalyticsRepository;
import com.swcampus.domain.analytics.BannerClickStats;
import com.swcampus.domain.analytics.EventStats;
import com.swcampus.domain.analytics.LectureClickStats;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Analytics Data API를 사용하여 Analytics 데이터를 조회하는 Repository 구현체
 */
@Repository
public class GoogleAnalyticsRepository implements AnalyticsRepository {

    private final BetaAnalyticsDataClient analyticsClient;
    private final String propertyId;

    public GoogleAnalyticsRepository(
            BetaAnalyticsDataClient analyticsClient,
            @Qualifier("googleAnalyticsPropertyId") String propertyId
    ) {
        this.analyticsClient = analyticsClient;
        this.propertyId = propertyId;
    }

    @Override
    public AnalyticsReport getReport(int daysAgo) {
        String startDate = daysAgo + "daysAgo";
        String endDate = "today";

        // 총 통계 데이터 조회
        RunReportResponse summaryResponse = analyticsClient.runReport(
            RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .build())
                .addMetrics(Metric.newBuilder().setName("totalUsers"))
                .addMetrics(Metric.newBuilder().setName("activeUsers"))
                .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                .addMetrics(Metric.newBuilder().setName("sessions"))
                .build()
        );

        long totalUsers = 0;
        long activeUsers = 0;
        long pageViews = 0;
        long sessions = 0;

        if (!summaryResponse.getRowsList().isEmpty()) {
            Row row = summaryResponse.getRows(0);
            totalUsers = Long.parseLong(row.getMetricValues(0).getValue());
            activeUsers = Long.parseLong(row.getMetricValues(1).getValue());
            pageViews = Long.parseLong(row.getMetricValues(2).getValue());
            sessions = Long.parseLong(row.getMetricValues(3).getValue());
        }

        // 일별 통계 데이터 조회
        RunReportResponse dailyResponse = analyticsClient.runReport(
            RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .build())
                .addDimensions(Dimension.newBuilder().setName("date"))
                .addMetrics(Metric.newBuilder().setName("activeUsers"))
                .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                .addOrderBys(OrderBy.newBuilder()
                    .setDimension(OrderBy.DimensionOrderBy.newBuilder()
                        .setDimensionName("date")
                        .build())
                    .build())
                .build()
        );

        List<AnalyticsReport.DailyStats> dailyStats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (Row row : dailyResponse.getRowsList()) {
            LocalDate date = LocalDate.parse(row.getDimensionValues(0).getValue(), formatter);
            long dailyActiveUsers = Long.parseLong(row.getMetricValues(0).getValue());
            long dailyPageViews = Long.parseLong(row.getMetricValues(1).getValue());
            dailyStats.add(new AnalyticsReport.DailyStats(date, dailyActiveUsers, dailyPageViews));
        }

        return new AnalyticsReport(totalUsers, activeUsers, pageViews, sessions, dailyStats);
    }

    @Override
    public EventStats getEventStats(int daysAgo) {
        String startDate = daysAgo + "daysAgo";
        String endDate = "today";

        // 1. 전체 이벤트 통계 조회
        RunReportResponse eventResponse = analyticsClient.runReport(
            RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .build())
                .addDimensions(Dimension.newBuilder().setName("eventName"))
                .addMetrics(Metric.newBuilder().setName("eventCount"))
                .build()
        );

        long bannerClicks = 0;
        long applyButtonClicks = 0;
        long shareClicks = 0;
        List<EventStats.EventDetail> eventDetails = new ArrayList<>();

        for (Row row : eventResponse.getRowsList()) {
            String eventName = row.getDimensionValues(0).getValue();
            long eventCount = Long.parseLong(row.getMetricValues(0).getValue());

            switch (eventName) {
                case "banner_click" -> bannerClicks = eventCount;
                case "apply_button_click" -> applyButtonClicks = eventCount;
                case "share" -> shareClicks = eventCount;
            }

            eventDetails.add(new EventStats.EventDetail(
                eventName,
                eventCount,
                null,
                null,
                null
            ));
        }

        // 2. Fetch banner stats by type (customEvent:banner_type)
        long bigBannerClicks = 0;
        long middleBannerClicks = 0;
        long smallBannerClicks = 0;

        try {
            RunReportResponse bannerResponse = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("customEvent:banner_type"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("eventName")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.EXACT)
                                .setValue("banner_click")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .build()
            );

            for (Row row : bannerResponse.getRowsList()) {
                String bannerType = row.getDimensionValues(0).getValue();
                long count = Long.parseLong(row.getMetricValues(0).getValue());

                switch (bannerType) {
                    case "BIG" -> bigBannerClicks = count;
                    case "MIDDLE" -> middleBannerClicks = count;
                    case "SMALL" -> smallBannerClicks = count;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch banner types: " + e.getMessage());
        }

        return new EventStats(
            bannerClicks, 
            bigBannerClicks, 
            middleBannerClicks, 
            smallBannerClicks,
            applyButtonClicks, 
            shareClicks, 
            eventDetails
        );
    }

    @Override
    public List<BannerClickStats> getTopBannersByClicks(int daysAgo, int limit) {
        String startDate = daysAgo + "daysAgo";
        String endDate = "today";
        List<BannerClickStats> result = new ArrayList<>();

        try {
            RunReportResponse response = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("customEvent:banner_id"))
                    .addDimensions(Dimension.newBuilder().setName("customEvent:banner_name"))
                    .addDimensions(Dimension.newBuilder().setName("customEvent:banner_type"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("eventName")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.EXACT)
                                .setValue("banner_click")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .addOrderBys(OrderBy.newBuilder()
                        .setMetric(OrderBy.MetricOrderBy.newBuilder()
                            .setMetricName("eventCount")
                            .build())
                        .setDesc(true)
                        .build())
                    .setLimit(limit)
                    .build()
            );

            for (Row row : response.getRowsList()) {
                String bannerId = row.getDimensionValues(0).getValue();
                String bannerName = row.getDimensionValues(1).getValue();
                String bannerType = row.getDimensionValues(2).getValue();
                long clickCount = Long.parseLong(row.getMetricValues(0).getValue());

                result.add(new BannerClickStats(bannerId, bannerName, bannerType, clickCount));
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch top banners: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<LectureClickStats> getTopLecturesByClicks(int daysAgo, int limit) {
        String startDate = daysAgo + "daysAgo";
        String endDate = "today";
        
        // Map to aggregate lecture stats
        Map<String, LectureData> lectureMap = new HashMap<>();

        try {
            // Fetch apply_button_click events
            RunReportResponse applyResponse = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("customEvent:lecture_id"))
                    .addDimensions(Dimension.newBuilder().setName("customEvent:lecture_name"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("eventName")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.EXACT)
                                .setValue("apply_button_click")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .build()
            );

            for (Row row : applyResponse.getRowsList()) {
                String lectureId = row.getDimensionValues(0).getValue();
                String lectureName = row.getDimensionValues(1).getValue();
                long count = Long.parseLong(row.getMetricValues(0).getValue());

                lectureMap.computeIfAbsent(lectureId, k -> new LectureData(lectureName))
                    .applyClicks = count;
            }

            // Fetch share events
            RunReportResponse shareResponse = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("customEvent:lecture_id"))
                    .addDimensions(Dimension.newBuilder().setName("customEvent:lecture_name"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("eventName")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.EXACT)
                                .setValue("share")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .build()
            );

            for (Row row : shareResponse.getRowsList()) {
                String lectureId = row.getDimensionValues(0).getValue();
                String lectureName = row.getDimensionValues(1).getValue();
                long count = Long.parseLong(row.getMetricValues(0).getValue());

                lectureMap.computeIfAbsent(lectureId, k -> new LectureData(lectureName))
                    .shareClicks = count;
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch top lectures: " + e.getMessage());
        }

        // Convert to result list and sort by total clicks
        return lectureMap.entrySet().stream()
            .map(entry -> new LectureClickStats(
                entry.getKey(),
                entry.getValue().lectureName,
                entry.getValue().applyClicks,
                entry.getValue().shareClicks,
                entry.getValue().applyClicks + entry.getValue().shareClicks
            ))
            .sorted(Comparator.comparingLong(LectureClickStats::totalClicks).reversed())
            .limit(limit)
            .toList();
    }

    // Helper class for aggregating lecture data
    private static class LectureData {
        String lectureName;
        long applyClicks = 0;
        long shareClicks = 0;

        LectureData(String lectureName) {
            this.lectureName = lectureName;
        }
    }
}


