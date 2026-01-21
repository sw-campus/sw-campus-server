package com.swcampus.domain.lecture.dto;

import com.swcampus.domain.lecture.Lecture;

public record LectureSummaryDto(
        Lecture lecture,
        Double averageScore,
        Long reviewCount) {
    public static LectureSummaryDto from(Lecture lecture, Double averageScore, Long reviewCount) {
        return new LectureSummaryDto(lecture, averageScore, reviewCount);
    }
}
