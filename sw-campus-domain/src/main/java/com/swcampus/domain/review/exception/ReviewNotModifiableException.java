package com.swcampus.domain.review.exception;

public class ReviewNotModifiableException extends RuntimeException {

    public ReviewNotModifiableException() {
        super("승인된 후기는 수정할 수 없습니다");
    }

    public ReviewNotModifiableException(Long reviewId) {
        super(String.format("승인된 후기는 수정할 수 없습니다. Review ID: %d", reviewId));
    }
}
