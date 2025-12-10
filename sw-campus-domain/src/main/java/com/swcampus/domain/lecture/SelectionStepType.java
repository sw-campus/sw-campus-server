package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectionStepType {
    DOCUMENT("서류심사"),
    INTERVIEW("면접"),
    CODING_TEST("코딩테스트"),
    PRE_TASK("사전과제");

    private final String description;
}
