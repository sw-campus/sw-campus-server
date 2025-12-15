package com.swcampus.domain.review.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException() {
        super("후기를 찾을 수 없습니다");
    }

    public ReviewNotFoundException(Long id) {
        super(String.format("후기를 찾을 수 없습니다. ID: %d", id));
    }
}
