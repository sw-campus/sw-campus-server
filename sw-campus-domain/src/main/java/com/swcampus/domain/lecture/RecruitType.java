package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecruitType {
    CARD_REQUIRED("Learning Card Required"),
    GENERAL("General");

    private final String description;
}
