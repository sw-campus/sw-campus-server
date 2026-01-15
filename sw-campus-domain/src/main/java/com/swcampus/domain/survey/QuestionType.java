package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionType {
    TEXT("텍스트 입력"),
    RADIO("단일 선택"),
    CHECKBOX("복수 선택"),
    RANGE("범위 선택"),
    CONDITIONAL("조건부 입력");

    private final String description;
}
