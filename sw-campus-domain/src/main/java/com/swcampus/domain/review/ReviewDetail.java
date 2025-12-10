package com.swcampus.domain.review;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewDetail {
    private Long id;
    private Long reviewId;
    private ReviewCategory category;
    private Double score;
    private String comment;

    public static ReviewDetail create(ReviewCategory category, Double score, String comment) {
        ReviewDetail detail = new ReviewDetail();
        detail.category = category;
        detail.score = score;
        detail.comment = comment;
        return detail;
    }

    public static ReviewDetail of(Long id, Long reviewId, ReviewCategory category,
                                   Double score, String comment) {
        ReviewDetail detail = new ReviewDetail();
        detail.id = id;
        detail.reviewId = reviewId;
        detail.category = category;
        detail.score = score;
        detail.comment = comment;
        return detail;
    }
}
