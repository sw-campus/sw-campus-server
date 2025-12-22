package com.swcampus.domain.lecture.exception;

public class LectureNotModifiableException extends RuntimeException {
    public LectureNotModifiableException() {
        super("승인 완료된 강의는 수정할 수 없습니다.");
    }
}
