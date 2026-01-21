package com.swcampus.domain.analytics;

/**
 * 인기 강의 통계 데이터
 */
public record PopularLecture(
    String lectureId,
    String lectureName,
    long views
) {}
