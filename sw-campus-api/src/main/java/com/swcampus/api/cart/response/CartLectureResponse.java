package com.swcampus.api.cart.response;

import com.swcampus.domain.lecture.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 강의 응답")
public record CartLectureResponse(
        @Schema(description = "강의 ID", example = "1") Long lectureId,
        @Schema(description = "강의명", example = "자바 스프링 부트 캠프") String lectureName,
        @Schema(description = "카테고리 ID", example = "10") Long categoryId) {

    public static CartLectureResponse from(Lecture lecture) {
        return new CartLectureResponse(
                lecture.getLectureId(),
                lecture.getLectureName(),
                lecture.extractCategoryId());
    }
}
