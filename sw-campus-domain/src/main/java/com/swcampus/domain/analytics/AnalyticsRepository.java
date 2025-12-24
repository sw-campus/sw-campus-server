package com.swcampus.domain.analytics;

import java.util.List;

/**
 * Analytics 데이터 조회를 위한 Repository 인터페이스 (Domain Layer)
 */
public interface AnalyticsRepository {
    
    /**
     * 지정된 기간의 Analytics 리포트를 조회합니다.
     *
     * @param daysAgo 며칠 전부터 오늘까지의 데이터를 조회할지 지정 (예: 7이면 최근 7일)
     * @return AnalyticsReport 통계 데이터
     */
    AnalyticsReport getReport(int daysAgo);
    
    /**
     * 지정된 기간의 이벤트 통계를 조회합니다.
     *
     * @param daysAgo 며칠 전부터 오늘까지의 데이터를 조회할지 지정
     * @return EventStats 이벤트 통계 데이터
     */
    EventStats getEventStats(int daysAgo);
    
    /**
     * 클릭 수 높은 순으로 배너 통계를 조회합니다.
     *
     * @param daysAgo 조회 기간
     * @param limit 조회할 개수
     * @return 배너별 클릭 통계 리스트
     */
    List<BannerClickStats> getTopBannersByClicks(int daysAgo, int limit);
    
    /**
     * 클릭 수 높은 순으로 강의 통계를 조회합니다.
     *
     * @param daysAgo 조회 기간
     * @param limit 조회할 개수
     * @return 강의별 클릭 통계 리스트
     */
    List<LectureClickStats> getTopLecturesByClicks(int daysAgo, int limit);
}


