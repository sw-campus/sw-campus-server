package com.swcampus.domain.common;

import java.util.Arrays;

/**
 * 승인 상태를 나타내는 공통 열거형.
 * 기관 승인, 리뷰 승인, 수료증 승인 등 다양한 도메인에서 사용됩니다.
 */
public enum ApprovalStatus {
    PENDING(0),   // 승인 대기
    APPROVED(1),  // 승인됨
    REJECTED(2);  // 반려됨

    private final int value;

    ApprovalStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 정수 값으로부터 ApprovalStatus를 반환합니다.
     *
     * @param value 상태 값 (0: PENDING, 1: APPROVED, 2: REJECTED)
     * @return 해당하는 ApprovalStatus
     * @throws IllegalArgumentException 알 수 없는 값인 경우
     */
    public static ApprovalStatus fromValue(int value) {
        for (ApprovalStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalStatus value: " + value);
    }

    /**
     * 문자열 이름으로부터 ApprovalStatus를 반환합니다. (대소문자 무시)
     *
     * @param name 상태 이름 (PENDING, APPROVED, REJECTED)
     * @return 해당하는 ApprovalStatus, 찾을 수 없으면 PENDING
     */
    public static ApprovalStatus fromName(String name) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(PENDING);
    }
}
