package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurriculumLevel {
    NONE("없음"),
    BASIC("기본"),
    ADVANCED("심화");

    private final String description;
}
