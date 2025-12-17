package com.swcampus.domain.organization;

import java.util.Arrays;

/**
 * 기관 승인 상태
 */
public enum ApprovalStatus {
    PENDING, // 승인 대기
    APPROVED, // 승인됨
    REJECTED; // 반려됨

    public static ApprovalStatus fromName(String name) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(PENDING);
    }
}
