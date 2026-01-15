package com.swcampus.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionSetStatus {
    DRAFT("작성 중"),
    PUBLISHED("발행됨"),
    ARCHIVED("보관됨");

    private final String description;

    public boolean isEditable() {
        return this == DRAFT;
    }
}
