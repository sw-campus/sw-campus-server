package com.swcampus.domain.review.exception;

public class ReviewNotOwnerException extends RuntimeException {

    public ReviewNotOwnerException() {
        super("본인의 후기만 수정할 수 있습니다");
    }
}
