package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 적성 등급 - 내부 활용 전용 (사용자에게 노출하지 않음).
 * AI 추천 시 난이도 조절에 참고 정보로만 사용.
 */
@Getter
@AllArgsConstructor
public enum AptitudeGrade {
    TALENTED(61, 80, "재능형"),
    DILIGENT(41, 60, "노력형"),
    EXPLORING(21, 40, "탐색형"),
    RECONSIDER(0, 20, "재고형");

    private final int minScore;
    private final int maxScore;
    private final String description;

    public static AptitudeGrade fromScore(int score) {
        for (AptitudeGrade grade : values()) {
            if (score >= grade.minScore && score <= grade.maxScore) {
                return grade;
            }
        }
        return RECONSIDER;
    }
}
