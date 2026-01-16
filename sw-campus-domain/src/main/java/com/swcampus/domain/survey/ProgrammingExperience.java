package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgrammingExperience {
    private boolean hasExperience;
    private String bootcampName;

    @Builder
    public ProgrammingExperience(boolean hasExperience, String bootcampName) {
        this.hasExperience = hasExperience;
        this.bootcampName = bootcampName;
    }

    public static ProgrammingExperience noExperience() {
        return new ProgrammingExperience(false, null);
    }

    public static ProgrammingExperience withExperience(String bootcampName) {
        return new ProgrammingExperience(true, bootcampName);
    }
}
