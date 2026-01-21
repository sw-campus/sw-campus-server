package com.swcampus.infra.analytics;

import com.google.analytics.data.v1beta.*;
import com.swcampus.domain.analytics.AnalyticsReport;
import com.swcampus.domain.analytics.AnalyticsRepository;
import com.swcampus.domain.analytics.BannerClickStats;
import com.swcampus.domain.analytics.EventStats;
import com.swcampus.domain.analytics.LectureClickStats;
import com.swcampus.domain.analytics.PopularLecture;
import com.swcampus.domain.analytics.PopularSearchTerm;
import com.swcampus.domain.analytics.TrafficSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.google.api.gax.rpc.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Google Analytics Data API를 사용하여 Analytics 데이터를 조회하는 Repository 구현체
 */
@Repository
@ConditionalOnProperty(name = "google.analytics.credentials-path")
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
        // daysAgo=1이면 오늘만, daysAgo=7이면 오늘 포함 7일 (6일 전부터)
        String startDate = (daysAgo - 1) + "daysAgo";
        String endDate = "today";

        // 병렬 실행: 3개의 GA API 요청을 동시에 실행
        CompletableFuture<RunReportResponse> summaryFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
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
            )
        );

        CompletableFuture<RunReportResponse> dailyFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
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
            )
        );

        CompletableFuture<RunReportResponse> deviceFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .build()
            )
        );

        // 모든 Future 완료 대기
        CompletableFuture.allOf(summaryFuture, dailyFuture, deviceFuture).join();

        // 결과 추출
        RunReportResponse summaryResponse = summaryFuture.join();
        RunReportResponse dailyResponse = dailyFuture.join();
        RunReportResponse deviceResponse = deviceFuture.join();

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

        List<AnalyticsReport.DailyStats> dailyStats = new ArrayList<>();

        for (Row row : dailyResponse.getRowsList()) {
            LocalDate date = LocalDate.parse(row.getDimensionValues(0).getValue(), DATE_FORMATTER);
            long dailyTotalUsers = Long.parseLong(row.getMetricValues(0).getValue());
            long dailyNewUsers = Long.parseLong(row.getMetricValues(1).getValue());
            long dailyPageViews = Long.parseLong(row.getMetricValues(2).getValue());
            dailyStats.add(new AnalyticsReport.DailyStats(date, dailyTotalUsers, dailyNewUsers, dailyPageViews));
        }

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
        String startDate = (daysAgo - 1) + "daysAgo";
        String endDate = "today";

        // 병렬 실행: 2개의 GA API 요청을 동시에 실행
        CompletableFuture<RunReportResponse> eventFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("eventName"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .build()
            )
        );

        CompletableFuture<RunReportResponse> bannerFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
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
            )
        );

        // 모든 Future 완료 대기
        CompletableFuture.allOf(eventFuture, bannerFuture).join();

        // 결과 추출
        RunReportResponse eventResponse = eventFuture.join();
        RunReportResponse bannerResponse = bannerFuture.join();

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

        long bigBannerClicks = 0;
        long middleBannerClicks = 0;
        long smallBannerClicks = 0;

        for (Row row : bannerResponse.getRowsList()) {
            String bannerType = row.getDimensionValues(0).getValue();
            long count = Long.parseLong(row.getMetricValues(0).getValue());

            switch (bannerType) {
                case "BIG" -> bigBannerClicks = count;
                case "MIDDLE" -> middleBannerClicks = count;
                case "SMALL" -> smallBannerClicks = count;
            }
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
        String startDate = (daysAgo - 1) + "daysAgo";
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
        String startDate = (daysAgo - 1) + "daysAgo";
        String endDate = "today";

        // 병렬 실행: 2개의 GA API 요청을 동시에 실행
        CompletableFuture<RunReportResponse> clickFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
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
            )
        );

        CompletableFuture<RunReportResponse> viewFuture = CompletableFuture.supplyAsync(() ->
            analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("pagePath"))
                    .addDimensions(Dimension.newBuilder().setName("pageTitle"))
                    .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("pagePath")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.BEGINS_WITH)
                                .setValue("/lectures/")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .setLimit(Math.max(limit * 3, 50))
                    .build()
            )
        );

        // 모든 Future 완료 대기
        CompletableFuture.allOf(clickFuture, viewFuture).join();

        // 결과 추출
        RunReportResponse clickResponse = clickFuture.join();
        RunReportResponse viewResponse = viewFuture.join();

        // Map to aggregate lecture stats
        Map<String, LectureData> lectureMap = new HashMap<>();

        for (Row row : clickResponse.getRowsList()) {
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

        for (Row row : viewResponse.getRowsList()) {
            String pagePath = row.getDimensionValues(0).getValue();
            String pageTitle = row.getDimensionValues(1).getValue();
            long views = Long.parseLong(row.getMetricValues(0).getValue());

            if (pagePath.matches("^/lectures/\\d+$")) {
                String lectureId = pagePath.replace("/lectures/", "");
                
                LectureData data = lectureMap.get(lectureId);
                if (data != null) {
                    data.views = views;
                } else {
                     String name = (pageTitle != null && !pageTitle.isEmpty() && !"(not set)".equals(pageTitle)) 
                                 ? pageTitle 
                                 : "강의 #" + lectureId;
                     
                     // Clean up title (remove " | SW Campus" etc if present)
                     if (name.contains(" |")) {
                        name = name.substring(0, name.indexOf(" |"));
                     }
                     
                     LectureData newData = new LectureData(name);
                     newData.views = views;
                     lectureMap.put(lectureId, newData);
                }
            }
        }

        // Convert to result list and sort by total clicks (descending), then views (descending)
        return lectureMap.entrySet().stream()
            .map(entry -> new LectureClickStats(
                entry.getKey(),
                entry.getValue().lectureName,
                entry.getValue().views,
                entry.getValue().applyClicks,
                entry.getValue().shareClicks,
                entry.getValue().applyClicks + entry.getValue().shareClicks
            ))
            .sorted(Comparator.comparingLong(LectureClickStats::totalClicks)
                .thenComparingLong(LectureClickStats::views)
                .reversed())
            .limit(limit)
            .toList();
    }

    // Helper class for aggregating lecture data
    private static class LectureData {
        String lectureName;
        long views = 0;
        long applyClicks = 0;
        long shareClicks = 0;

        LectureData(String lectureName) {
            this.lectureName = lectureName;
        }
    }

    @Override
    public List<PopularLecture> getPopularLectures(int daysAgo, int limit) {
        String startDate = (daysAgo - 1) + "daysAgo";
        String endDate = "today";
        List<PopularLecture> result = new ArrayList<>();

        try {
            // 강의 상세 페이지 조회수를 pagePath 기준으로 집계 (pageTitle 포함)
            RunReportResponse response = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("pagePath"))
                    .addDimensions(Dimension.newBuilder().setName("pageTitle"))
                    .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("pagePath")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.BEGINS_WITH)
                                .setValue("/lectures/")
                                .setCaseSensitive(false)
                            )
                        )
                        .build())
                    .addOrderBys(OrderBy.newBuilder()
                        .setMetric(OrderBy.MetricOrderBy.newBuilder()
                            .setMetricName("screenPageViews")
                            .build())
                        .setDesc(true)
                        .build())
                    .setLimit(limit + 10) // 여유분 확보 (search 페이지 등 제외용)
                    .build()
            );

            for (Row row : response.getRowsList()) {
                String pagePath = row.getDimensionValues(0).getValue();
                String pageTitle = row.getDimensionValues(1).getValue();
                long views = Long.parseLong(row.getMetricValues(0).getValue());

                // /lectures/123 형태만 추출 (search, category 등 제외)
                if (pagePath.matches("^/lectures/\\d+$")) {
                    String lectureId = pagePath.replace("/lectures/", "");
                    
                    String lectureName = (pageTitle != null && !pageTitle.isEmpty() && !"(not set)".equals(pageTitle)) 
                                         ? pageTitle 
                                         : "강의 #" + lectureId;
                    
                    // Clean up title (remove " | SW Campus" etc if present)
                    if (lectureName.contains(" |")) {
                        lectureName = lectureName.substring(0, lectureName.indexOf(" |"));
                    }
                    
                    result.add(new PopularLecture(lectureId, lectureName, views));
                    
                    if (result.size() >= limit) break;
                }
            }
        } catch (ApiException e) {
            log.error("Failed to fetch popular lectures", e);
        }

        return result;
    }

    @Override
    public List<PopularSearchTerm> getPopularSearchTerms(int daysAgo, int limit) {
        String startDate = (daysAgo - 1) + "daysAgo";
        String endDate = "today";
        List<PopularSearchTerm> result = new ArrayList<>();

        try {
            RunReportResponse response = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("customEvent:search_term"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .setDimensionFilter(FilterExpression.newBuilder()
                        .setFilter(Filter.newBuilder()
                            .setFieldName("eventName")
                            .setStringFilter(Filter.StringFilter.newBuilder()
                                .setMatchType(Filter.StringFilter.MatchType.EXACT)
                                .setValue("search")
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
                String term = row.getDimensionValues(0).getValue();
                long count = Long.parseLong(row.getMetricValues(0).getValue());
                
                // 빈 검색어 제외
                if (term != null && !term.isBlank() && !"(not set)".equals(term)) {
                    result.add(new PopularSearchTerm(term, count));
                }
            }
        } catch (ApiException e) {
            log.error("Failed to fetch popular search terms", e);
        }

        return result;
    }

    @Override
    public List<TrafficSource> getTrafficSources(int daysAgo, int limit) {
        String startDate = (daysAgo - 1) + "daysAgo";
        String endDate = "today";
        List<TrafficSource> result = new ArrayList<>();

        try {
            RunReportResponse response = analyticsClient.runReport(
                RunReportRequest.newBuilder()
                    .setProperty("properties/" + propertyId)
                    .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build())
                    .addDimensions(Dimension.newBuilder().setName("sessionSource"))
                    .addDimensions(Dimension.newBuilder().setName("sessionMedium"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("totalUsers"))
                    .addOrderBys(OrderBy.newBuilder()
                        .setMetric(OrderBy.MetricOrderBy.newBuilder()
                            .setMetricName("sessions")
                            .build())
                        .setDesc(true)
                        .build())
                    .setLimit(limit)
                    .build()
            );

            // 헤더 기반 인덱스 조회로 순서 변경에 안전하게 대응
            int sourceIndex = findDimensionIndex(response, "sessionSource");
            int mediumIndex = findDimensionIndex(response, "sessionMedium");
            int sessionsIndex = findMetricIndex(response, "sessions");
            int usersIndex = findMetricIndex(response, "totalUsers");

            for (Row row : response.getRowsList()) {
                String source = row.getDimensionValues(sourceIndex).getValue();
                String medium = row.getDimensionValues(mediumIndex).getValue();
                long sessions = Long.parseLong(row.getMetricValues(sessionsIndex).getValue());
                long users = Long.parseLong(row.getMetricValues(usersIndex).getValue());

                result.add(new TrafficSource(source, medium, sessions, users));
            }
        } catch (ApiException e) {
            log.error("Failed to fetch traffic sources", e);
        }

        return result;
    }

    /**
     * 응답 헤더에서 dimension 이름으로 인덱스를 찾습니다.
     * @param response GA API 응답
     * @param dimensionName 찾을 dimension 이름
     * @return dimension 인덱스 (찾지 못하면 -1)
     */
    private int findDimensionIndex(RunReportResponse response, String dimensionName) {
        List<DimensionHeader> headers = response.getDimensionHeadersList();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getName().equals(dimensionName)) {
                return i;
            }
        }
        log.warn("Dimension '{}' not found in response headers", dimensionName);
        return -1;
    }

    /**
     * 응답 헤더에서 metric 이름으로 인덱스를 찾습니다.
     * @param response GA API 응답
     * @param metricName 찾을 metric 이름
     * @return metric 인덱스 (찾지 못하면 -1)
     */
    private int findMetricIndex(RunReportResponse response, String metricName) {
        List<MetricHeader> headers = response.getMetricHeadersList();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getName().equals(metricName)) {
                return i;
            }
        }
        log.warn("Metric '{}' not found in response headers", metricName);
        return -1;
    }
}


