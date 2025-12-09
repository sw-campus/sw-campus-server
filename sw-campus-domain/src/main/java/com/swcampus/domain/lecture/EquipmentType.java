package com.swcampus.domain.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EquipmentType {
    NONE("None"),
    MAC("Mac"),
    WINDOWS("Windows");

    private final String description;
}
