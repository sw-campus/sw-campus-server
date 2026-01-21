package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 추천 직무 - Part 3 응답 기반으로 결정.
 * 사용자에게 표시되는 유일한 결과.
 */
@Getter
@AllArgsConstructor
public enum RecommendedJob {
    FRONTEND("프론트엔드 개발자"),
    BACKEND("백엔드 개발자"),
    DATA("데이터 분석가 / AI 엔지니어"),
    FULLSTACK("풀스택 개발자");

    private final String description;
}
