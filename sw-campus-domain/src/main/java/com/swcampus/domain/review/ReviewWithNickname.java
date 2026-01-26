package com.swcampus.domain.review;

/**
 * 리뷰와 작성자 닉네임을 함께 담는 도메인 객체
 */
public record ReviewWithNickname(
    Review review,
    Long memberId,
    String nickname
) {
    private static final String UNKNOWN_AUTHOR = "알 수 없음";

    public static ReviewWithNickname of(Review review, String nickname) {
        Long memberId = review.getMemberId();
        String displayNickname = (memberId == null || nickname == null || nickname.isBlank())
                ? UNKNOWN_AUTHOR
                : nickname;
        return new ReviewWithNickname(review, memberId, displayNickname);
    }
}
