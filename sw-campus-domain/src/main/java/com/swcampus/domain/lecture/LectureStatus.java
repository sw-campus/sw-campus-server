package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureStatus {
    RECRUITING("모집중"),
    ONGOING("진행중"),
    FINISHED("종료");

    private final String description;
}
