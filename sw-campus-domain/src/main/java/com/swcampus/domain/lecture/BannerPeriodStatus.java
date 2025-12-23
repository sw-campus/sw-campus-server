package com.swcampus.domain.lecture;

/**
 * 배너 기간 상태를 나타내는 열거형
 * 배너가 현재 시점 기준으로 어떤 상태인지를 나타냅니다.
 */
public enum BannerPeriodStatus {
    /**
     * 예정 - 시작일이 현재보다 미래인 배너
     */
    SCHEDULED,
    
    /**
     * 진행중 - 현재 시점이 시작일과 종료일 사이인 배너
     */
    ACTIVE,
    
    /**
     * 종료 - 종료일이 현재보다 과거인 배너
     */
    ENDED;
    
    /**
     * 문자열로부터 BannerPeriodStatus를 반환합니다.
     * null이나 빈 문자열인 경우 null을 반환합니다.
     * 
     * @param value 상태 문자열
     * @return BannerPeriodStatus 또는 null
     */
    public static BannerPeriodStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return valueOf(value.toUpperCase());
    }
}
