package com.swcampus.domain.analytics;

/**
 * 강의별 클릭 통계
 */
public record LectureClickStats(
    String lectureId,
    String lectureName,
    long views,
    long applyClicks,
    long shareClicks,
    long totalClicks
) {}

