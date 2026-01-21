package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DesiredJob {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    DATA("데이터"),
    AI("AI"),
    MOBILE("모바일"),
    OTHER("기타");

    private final String description;
}
