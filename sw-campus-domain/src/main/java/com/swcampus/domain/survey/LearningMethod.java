package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LearningMethod {
    ONLINE("온라인"),
    OFFLINE("오프라인"),
    MIXED("혼합");

    private final String description;
}
