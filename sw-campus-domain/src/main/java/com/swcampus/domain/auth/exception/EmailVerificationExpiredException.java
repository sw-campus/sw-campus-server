package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class EmailVerificationExpiredException extends BusinessException {

	public EmailVerificationExpiredException() {
		super(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
	}

	public EmailVerificationExpiredException(String email) {
		super(ErrorCode.EMAIL_VERIFICATION_EXPIRED, String.format("인증 코드가 만료되었습니다: %s", email));
	}
}
