package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureStatus {
    RECRUITING("모집중"),
    FINISHED("모집 종료");

    private final String description;
}
