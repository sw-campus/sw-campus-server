package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionSetType {
    BASIC("기초 설문"),
    APTITUDE("성향 테스트");

    private final String description;
}
