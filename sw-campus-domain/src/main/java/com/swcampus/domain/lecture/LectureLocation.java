package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureLocation {
    ONLINE("Online"),
    OFFLINE("Offline"),
    MIXED("Mixed");

    private final String description;
}
