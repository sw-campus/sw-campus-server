package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class TokenExpiredException extends BusinessException {

	public TokenExpiredException() {
		super(ErrorCode.TOKEN_EXPIRED);
	}

	public TokenExpiredException(String message) {
		super(ErrorCode.TOKEN_EXPIRED, message);
	}
}
