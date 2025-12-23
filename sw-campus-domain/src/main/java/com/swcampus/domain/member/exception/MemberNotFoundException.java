package com.swcampus.domain.member.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("회원을 찾을 수 없습니다");
    }

    public MemberNotFoundException(Long id) {
        super(String.format("회원을 찾을 수 없습니다. ID: %d", id));
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
