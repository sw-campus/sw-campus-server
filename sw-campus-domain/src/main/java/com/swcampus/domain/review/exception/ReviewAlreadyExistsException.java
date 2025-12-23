package com.swcampus.domain.review.exception;

public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException() {
        super("이미 후기를 작성한 강의입니다");
    }

    public ReviewAlreadyExistsException(Long memberId, Long lectureId) {
        super(String.format("이미 후기를 작성한 강의입니다. memberId: %d, lectureId: %d", memberId, lectureId));
    }
}
