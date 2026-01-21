package com.swcampus.domain.ratelimit.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class RateLimitExceededException extends BusinessException {

	public RateLimitExceededException() {
		super(ErrorCode.RATE_LIMIT_EXCEEDED);
	}

	public RateLimitExceededException(String message) {
		super(ErrorCode.RATE_LIMIT_EXCEEDED, message);
	}
}
