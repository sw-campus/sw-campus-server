package com.swcampus.api.cart.response;

import com.swcampus.domain.lecture.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 강의 응답")
public record CartLectureResponse(
        @Schema(description = "강의 ID", example = "1") Long lectureId,
        @Schema(description = "강의명", example = "자바 스프링 부트 캠프") String lectureName,
        @Schema(description = "카테고리 ID", example = "10") Long categoryId,
        @Schema(description = "카테고리명", example = "백엔드") String categoryName,
        @Schema(description = "기관명", example = "패스트캠퍼스") String orgName,
        @Schema(description = "강의 대표 이미지 URL", example = "https://example.com/images/lecture1.jpg") String lectureImageUrl) {

    public static CartLectureResponse from(Lecture lecture) {
        return new CartLectureResponse(
                lecture.getLectureId(),
                lecture.getLectureName(),
                lecture.extractCategoryId(),
                extractCategoryName(lecture),
                lecture.getOrgName(),
                lecture.getLectureImageUrl());
    }

    /**
     * categoryName 추출 (JPA 조회 시 lectureCurriculums에서 추출)
     */
    private static String extractCategoryName(Lecture lecture) {
        // 이미 설정되어 있으면 (MyBatis 검색 결과 등) 사용
        if (lecture.getCategoryName() != null && !lecture.getCategoryName().isBlank()) {
            return lecture.getCategoryName();
        }

        // lectureCurriculums에서 추출 (JPA 상세 조회)
        if (lecture.getLectureCurriculums() == null || lecture.getLectureCurriculums().isEmpty()) {
            return null;
        }
        var firstCurriculum = lecture.getLectureCurriculums().get(0).getCurriculum();
        if (firstCurriculum == null || firstCurriculum.getCategory() == null) {
            return null;
        }
        return firstCurriculum.getCategory().getCategoryName();
    }
}
