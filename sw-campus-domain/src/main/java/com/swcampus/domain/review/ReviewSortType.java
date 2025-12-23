package com.swcampus.domain.review;

import org.springframework.data.domain.Sort;

public enum ReviewSortType {
    LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt")),
    SCORE_DESC(Sort.by(Sort.Direction.DESC, "score")),
    SCORE_ASC(Sort.by(Sort.Direction.ASC, "score"));

    private final Sort sort;

    ReviewSortType(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
