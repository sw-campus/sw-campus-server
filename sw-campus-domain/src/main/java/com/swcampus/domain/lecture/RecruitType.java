package com.swcampus.domain.lecture;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecruitType {
    CARD_REQUIRED("내일배움카드 필요"),
    GENERAL("내일배움카드 불필요");

    private final String description;
}
