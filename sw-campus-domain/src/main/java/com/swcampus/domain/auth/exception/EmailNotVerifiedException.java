package com.swcampus.domain.auth.exception;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("이메일 인증이 완료되지 않았습니다");
    }

    public EmailNotVerifiedException(String email) {
        super(String.format("이메일 인증이 완료되지 않았습니다: %s", email));
    }
}
