package com.swcampus.domain.auth.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("이미 가입된 이메일입니다");
    }

    public DuplicateEmailException(String email) {
        super(String.format("이미 가입된 이메일입니다: %s", email));
    }
}
