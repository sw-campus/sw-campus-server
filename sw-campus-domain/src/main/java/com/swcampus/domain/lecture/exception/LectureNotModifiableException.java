package com.swcampus.domain.lecture.exception;

public class LectureNotModifiableException extends RuntimeException {
    public LectureNotModifiableException() {
        super("반려된 강의만 수정할 수 있습니다.");
    }
}
