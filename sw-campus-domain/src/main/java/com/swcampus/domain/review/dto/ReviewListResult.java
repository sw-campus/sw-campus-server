package com.swcampus.domain.review.dto;

import java.util.List;

import com.swcampus.domain.review.ReviewWithNickname;

/**
 * 블라인드 필터링이 적용된 리뷰 목록 결과.
 * 
 * @param reviews 필터링된 리뷰 목록 (블라인드 상태면 1개, 해제면 전체)
 * @param totalCount 전체 리뷰 개수 (블라인드 무관)
 * @param isUnblinded 블라인드 해제 여부
 */
public record ReviewListResult(
    List<ReviewWithNickname> reviews,
    int totalCount,
    boolean isUnblinded
) {
    public static ReviewListResult of(
            List<ReviewWithNickname> reviews,
            int totalCount,
            boolean isUnblinded) {
        return new ReviewListResult(reviews, totalCount, isUnblinded);
    }
}
