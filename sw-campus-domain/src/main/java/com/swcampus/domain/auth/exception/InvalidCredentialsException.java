package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class InvalidCredentialsException extends BusinessException {

	public InvalidCredentialsException() {
		super(ErrorCode.INVALID_CREDENTIALS);
	}
}
