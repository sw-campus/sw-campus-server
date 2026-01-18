package com.swcampus.domain.survey;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MajorInfo {
    private boolean hasMajor;
    private String majorName;

    @Builder
    public MajorInfo(boolean hasMajor, String majorName) {
        this.hasMajor = hasMajor;
        this.majorName = majorName;
    }

    public static MajorInfo noMajor() {
        return new MajorInfo(false, null);
    }

    public static MajorInfo withMajor(String majorName) {
        return new MajorInfo(true, majorName);
    }
}
