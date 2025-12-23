package com.swcampus.domain.auth.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException() {
        super("토큰이 만료되었습니다");
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}
