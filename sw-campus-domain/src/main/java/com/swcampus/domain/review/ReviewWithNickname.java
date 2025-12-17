package com.swcampus.domain.review;

/**
 * 리뷰와 작성자 닉네임을 함께 담는 도메인 객체
 */
public record ReviewWithNickname(
    Review review,
    String nickname
) {
    public static ReviewWithNickname of(Review review, String nickname) {
        return new ReviewWithNickname(review, nickname != null ? nickname : "");
    }
}
