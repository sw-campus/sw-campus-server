package com.swcampus.domain.review;

/**
 * 리뷰 관련 유효성 검사 상수 정의
 */
public final class ReviewValidationConstants {
    
    private ReviewValidationConstants() {
        // 인스턴스화 방지
    }

    /**
     * 리뷰 총평 최대 글자 수
     */
    public static final int REVIEW_COMMENT_MAX_LENGTH = 250;

    /**
     * 카테고리별 상세 후기 최소 글자 수
     */
    public static final int DETAIL_COMMENT_MIN_LENGTH = 10;

    /**
     * 카테고리별 상세 후기 최대 글자 수
     */
    public static final int DETAIL_COMMENT_MAX_LENGTH = 250;

    /**
     * 상세 점수 카테고리 개수
     */
    public static final int DETAIL_SCORE_CATEGORY_COUNT = 5;

    /**
     * 점수 최소값
     */
    public static final String SCORE_MIN = "1.0";

    /**
     * 점수 최대값
     */
    public static final String SCORE_MAX = "5.0";
}
