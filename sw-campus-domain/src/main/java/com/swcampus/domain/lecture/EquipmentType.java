package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EquipmentType {
    NONE("없음"),
    PC("PC"),
    LAPTOP("노트북"),
    PERSONAL("개인장비");

    private final String description;
}
