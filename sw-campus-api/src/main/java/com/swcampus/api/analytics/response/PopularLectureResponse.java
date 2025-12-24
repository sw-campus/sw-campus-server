package com.swcampus.api.analytics.response;

import java.util.List;

import com.swcampus.domain.analytics.PopularLecture;

public record PopularLectureResponse(
    String lectureId,
    String lectureName,
    long views
) {
    public static PopularLectureResponse from(PopularLecture lecture) {
        return new PopularLectureResponse(
            lecture.lectureId(),
            lecture.lectureName(),
            lecture.views()
        );
    }
    
    public static List<PopularLectureResponse> fromList(List<PopularLecture> lectures) {
        return lectures.stream().map(PopularLectureResponse::from).toList();
    }
}
