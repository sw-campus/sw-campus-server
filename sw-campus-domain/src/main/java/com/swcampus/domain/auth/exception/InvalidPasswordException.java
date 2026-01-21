package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class InvalidPasswordException extends BusinessException {

	public InvalidPasswordException() {
		super(ErrorCode.INVALID_PASSWORD);
	}

	public InvalidPasswordException(String message) {
		super(ErrorCode.INVALID_PASSWORD, message);
	}
}
