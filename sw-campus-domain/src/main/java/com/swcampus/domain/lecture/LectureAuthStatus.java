package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureAuthStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("승인 반려");

    private final String description;
}
