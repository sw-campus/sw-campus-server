package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BudgetRange {
    UNDER_50("50만원 미만"),
    RANGE_50_100("50~100만원"),
    RANGE_100_200("100~200만원"),
    OVER_200("200만원 이상");

    private final String description;
}
