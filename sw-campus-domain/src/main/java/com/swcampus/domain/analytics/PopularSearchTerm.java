package com.swcampus.domain.analytics;

/**
 * 인기 검색어 통계 데이터
 */
public record PopularSearchTerm(
    String term,
    long count
) {}
