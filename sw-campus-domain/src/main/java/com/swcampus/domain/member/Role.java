package com.swcampus.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("일반 사용자"),

    ORGANIZATION("기관"),

    ADMIN("관리자");

    private final String description;
}
