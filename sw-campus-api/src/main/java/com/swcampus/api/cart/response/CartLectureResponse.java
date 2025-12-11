package com.swcampus.api.cart.response;

import com.swcampus.domain.lecture.Lecture;

public record CartLectureResponse(
        Long lectureId,
        String lectureName) {

    public static CartLectureResponse from(Lecture lecture) {
        return new CartLectureResponse(lecture.getLectureId(), lecture.getLectureName());
    }
}
