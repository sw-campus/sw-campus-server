package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Part 3 문항의 직무 유형 코드.
 */
@Getter
@AllArgsConstructor
public enum JobTypeCode {
    F("프론트엔드"),
    B("백엔드"),
    D("데이터/AI");

    private final String description;

    public RecommendedJob toRecommendedJob() {
        return switch (this) {
            case F -> RecommendedJob.FRONTEND;
            case B -> RecommendedJob.BACKEND;
            case D -> RecommendedJob.DATA;
        };
    }
}
