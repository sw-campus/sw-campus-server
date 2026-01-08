package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class InvalidTokenException extends BusinessException {

	public InvalidTokenException() {
		super(ErrorCode.INVALID_TOKEN);
	}

	public InvalidTokenException(String message) {
		super(ErrorCode.INVALID_TOKEN, message);
	}
}
