package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 성향 테스트 문항의 파트 구분.
 */
@Getter
@AllArgsConstructor
public enum QuestionPart {
    PART1("논리 및 사고력"),
    PART2("끈기 및 학습 태도"),
    PART3("직무 성향 정밀 진단");

    private final String description;
}
