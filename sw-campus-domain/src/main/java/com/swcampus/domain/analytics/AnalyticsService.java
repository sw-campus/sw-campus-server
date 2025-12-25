package com.swcampus.domain.analytics;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Analytics 서비스 (Domain Layer)
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
    public EventStats getEventStats(int daysAgo) {
        if (daysAgo <= 0) {
            daysAgo = DEFAULT_DAYS;
        }
        return analyticsRepository.getEventStats(daysAgo);
    }
    
    /**
     * 클릭 수 높은 순으로 배너 통계를 조회합니다.
     */
    public List<BannerClickStats> getTopBannersByClicks(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getTopBannersByClicks(daysAgo, limit);
    }
    
    /**
     * 클릭 수 높은 순으로 강의 통계를 조회합니다.
     */
    public List<LectureClickStats> getTopLecturesByClicks(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getTopLecturesByClicks(daysAgo, limit);
    }
    
    /**
     * 페이지 조회수 기준 인기 강의 목록을 조회합니다.
     */
    public List<PopularLecture> getPopularLectures(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getPopularLectures(daysAgo, limit);
    }
    
    /**
     * 검색 횟수 기준 인기 검색어 목록을 조회합니다.
     */
    public List<PopularSearchTerm> getPopularSearchTerms(int daysAgo, int limit) {
        if (daysAgo <= 0) daysAgo = DEFAULT_DAYS;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        return analyticsRepository.getPopularSearchTerms(daysAgo, limit);
    }
}


