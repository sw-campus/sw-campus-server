package com.swcampus.domain.auth.exception;

public class EmailVerificationExpiredException extends RuntimeException {

    public EmailVerificationExpiredException() {
        super("인증 코드가 만료되었습니다");
    }

    public EmailVerificationExpiredException(String email) {
        super(String.format("인증 코드가 만료되었습니다: %s", email));
    }
}
