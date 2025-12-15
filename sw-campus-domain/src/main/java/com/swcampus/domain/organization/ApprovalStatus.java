package com.swcampus.domain.organization;

import java.util.Arrays;

/**
 * 기관 승인 상태
 */
public enum ApprovalStatus {
    PENDING(0), // 승인 대기
    APPROVED(1), // 승인됨
    REJECTED(2); // 반려됨

    private final int value;

    ApprovalStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ApprovalStatus fromValue(int value) {
        return Arrays.stream(values())
                .filter(s -> s.value == value)
                .findFirst()
                .orElse(PENDING);
    }
}
