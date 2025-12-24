package com.swcampus.api.analytics.response;

import java.util.List;

import com.swcampus.domain.analytics.LectureClickStats;

public record LectureClickStatsResponse(
    String lectureId,
    String lectureName,
    long applyClicks,
    long shareClicks,
    long totalClicks
) {
    public static LectureClickStatsResponse from(LectureClickStats stats) {
        return new LectureClickStatsResponse(
            stats.lectureId(),
            stats.lectureName(),
            stats.applyClicks(),
            stats.shareClicks(),
            stats.totalClicks()
        );
    }

    public static List<LectureClickStatsResponse> fromList(List<LectureClickStats> statsList) {
        return statsList.stream().map(LectureClickStatsResponse::from).toList();
    }
}
