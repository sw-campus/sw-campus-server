package com.swcampus.infra.analytics;

import com.google.analytics.data.v1beta.*;
import com.swcampus.domain.analytics.AnalyticsReport;
import com.swcampus.domain.analytics.AnalyticsRepository;
import com.swcampus.domain.analytics.BannerClickStats;
import com.swcampus.domain.analytics.EventStats;
import com.swcampus.domain.analytics.LectureClickStats;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.google.api.gax.rpc.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(GoogleAnalyticsRepository.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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
                .addMetrics(Metric.newBuilder().setName("newUsers"))
                .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                .addMetrics(Metric.newBuilder().setName("sessions"))
                .build()
        );

        long totalUsers = 0;
        long activeUsers = 0;
        long newUsers = 0;
        double averageEngagementTime = 0.0;
        long pageViews = 0;
        long sessions = 0;

        if (!summaryResponse.getRowsList().isEmpty()) {
            Row row = summaryResponse.getRows(0);
            totalUsers = Long.parseLong(row.getMetricValues(0).getValue());
            activeUsers = Long.parseLong(row.getMetricValues(1).getValue());
            newUsers = Long.parseLong(row.getMetricValues(2).getValue());
            averageEngagementTime = Double.parseDouble(row.getMetricValues(3).getValue());
            pageViews = Long.parseLong(row.getMetricValues(4).getValue());
            sessions = Long.parseLong(row.getMetricValues(5).getValue());
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
                .addMetrics(Metric.newBuilder().setName("totalUsers"))
                .addMetrics(Metric.newBuilder().setName("newUsers"))
                .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                .addOrderBys(OrderBy.newBuilder()
                    .setDimension(OrderBy.DimensionOrderBy.newBuilder()
                        .setDimensionName("date")
                        .build())
                    .build())
                .build()
        );

        List<AnalyticsReport.DailyStats> dailyStats = new ArrayList<>();

        for (Row row : dailyResponse.getRowsList()) {
            LocalDate date = LocalDate.parse(row.getDimensionValues(0).getValue(), DATE_FORMATTER);
            long dailyTotalUsers = Long.parseLong(row.getMetricValues(0).getValue());
            long dailyNewUsers = Long.parseLong(row.getMetricValues(1).getValue());
            long dailyPageViews = Long.parseLong(row.getMetricValues(2).getValue());
            dailyStats.add(new AnalyticsReport.DailyStats(date, dailyTotalUsers, dailyNewUsers, dailyPageViews));
        }

        // 기기별 통계 데이터 조회 (New Request)
        RunReportResponse deviceResponse = analyticsClient.runReport(
            RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .build())
                .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
                .addMetrics(Metric.newBuilder().setName("activeUsers"))
                .build()
        );

        List<AnalyticsReport.DeviceStat> deviceStats = new ArrayList<>();
        for (Row row : deviceResponse.getRowsList()) {
            String category = row.getDimensionValues(0).getValue();
            long users = Long.parseLong(row.getMetricValues(0).getValue());
            deviceStats.add(new AnalyticsReport.DeviceStat(category, users));
        }

        return new AnalyticsReport(totalUsers, activeUsers, newUsers, averageEngagementTime, pageViews, sessions, dailyStats, deviceStats);
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
                eventCount
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
        } catch (ApiException e) {
            log.error("Failed to fetch banner types", e);
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
        } catch (ApiException e) {
            log.error("Failed to fetch top banners", e);
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
            // Fetch both apply_button_click and share events in a single API call
            RunReportResponse response = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("eventName"))
                    .addDimensions(Dimension.newBuilder().setName("customEvent:lecture_id"))
                    .addDimensions(Dimension.newBuilder().setName("customEvent:lecture_name"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("eventName")
                            .setInListFilter(Filter.InListFilter.newBuilder()
                                .addValues("apply_button_click")
                                .addValues("share")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .build()
            );

            for (Row row : response.getRowsList()) {
                String eventName = row.getDimensionValues(0).getValue();
                String lectureId = row.getDimensionValues(1).getValue();
                String lectureName = row.getDimensionValues(2).getValue();
                long count = Long.parseLong(row.getMetricValues(0).getValue());

                LectureData data = lectureMap.computeIfAbsent(lectureId, k -> new LectureData(lectureName));
                if ("apply_button_click".equals(eventName)) {
                    data.applyClicks = count;
                } else if ("share".equals(eventName)) {
                    data.shareClicks = count;
                }
            }

        } catch (ApiException e) {
            log.error("Failed to fetch top lectures", e);
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


