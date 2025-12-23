package com.swcampus.domain.ratelimit.exception;

public class RateLimitExceededException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";

    public RateLimitExceededException() {
        super(DEFAULT_MESSAGE);
    }

    public RateLimitExceededException(String message) {
        super(message);
    }
}
