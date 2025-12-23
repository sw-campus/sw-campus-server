package com.swcampus.domain.lecture.dto;

public enum LectureSortType {
    LATEST, // 최신순 (기본)
    FEE_ASC, // 자기부담금 낮은 순
    FEE_DESC, // 자기부담금 높은 순
    START_SOON, // 개강 빠른 순
    DURATION_ASC, // 교육기간 짧은 순
    DURATION_DESC, // 교육기간 긴 순
    REVIEW_COUNT_DESC, // 리뷰 많은 순
    SCORE_DESC // 리뷰 평점 높은 순
}
