package com.swcampus.domain.analytics;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Analytics 서비스 (Domain Layer)
 * 
 * Caffeine 캐시를 사용하여 GA4 API 호출 결과를 5분간 캐싱합니다.
 */
@Service
public class AnalyticsService {

    private static final int DEFAULT_DAYS = 7;
    private static final int DEFAULT_LIMIT = 10;
    
    private final AnalyticsRepository analyticsRepository;
    
    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }
    
    /**
     * 최근 N일간의 Analytics 리포트를 조회합니다.
     *
     * @param daysAgo 조회할 일수 (기본값: 7)
     * @return AnalyticsReport
     */
    @Cacheable(value = "analyticsReport", key = "#p0")
    public AnalyticsReport getReport(int daysAgo) {
        if (daysAgo <= 0) {
            daysAgo = DEFAULT_DAYS;
        }
        return analyticsRepository.getReport(daysAgo);
    }
    
    /**
     * 최근 N일간의 이벤트 통계를 조회합니다.
     *
     * @param daysAgo 조회할 일수 (기본값: 7)
     * @return EventStats
     */
    @Cacheable(value = "eventStats", key = "#p0")
    public EventStats getEventStats(int daysAgo) {
        if (daysAgo <= 0) {
            daysAgo = DEFAULT_DAYS;
        }
        return analyticsRepository.getEventStats(daysAgo);
    }
    
    /**
     * 클릭 수 높은 순으로 배너 통계를 조회합니다.
     */
    @Cacheable(value = "topBanners", key = "#p0 + '-' + #p1")
    public List<BannerClickStats> getTopBannersByClicks(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getTopBannersByClicks(daysAgo, limit);
    }
    
    /**
     * 클릭 수 높은 순으로 강의 통계를 조회합니다.
     */
    @Cacheable(value = "topLectures", key = "#p0 + '-' + #p1")
    public List<LectureClickStats> getTopLecturesByClicks(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getTopLecturesByClicks(daysAgo, limit);
    }
    
    /**
     * 페이지 조회수 기준 인기 강의 목록을 조회합니다.
     */
    @Cacheable(value = "popularLectures", key = "#p0 + '-' + #p1")
    public List<PopularLecture> getPopularLectures(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getPopularLectures(daysAgo, limit);
    }
    
    /**
     * 검색 횟수 기준 인기 검색어 목록을 조회합니다.
     */
    @Cacheable(value = "popularSearchTerms", key = "#p0 + '-' + #p1")
    public List<PopularSearchTerm> getPopularSearchTerms(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getPopularSearchTerms(daysAgo, limit);
    }
    
    /**
     * 트래픽 소스별 세션/사용자 통계를 조회합니다.
     */
    @Cacheable(value = "trafficSources", key = "#p0 + '-' + #p1")
    public List<TrafficSource> getTrafficSources(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getTrafficSources(daysAgo, limit);
    }
}



