package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class EmailNotVerifiedException extends BusinessException {

	public EmailNotVerifiedException() {
		super(ErrorCode.EMAIL_NOT_VERIFIED);
	}

	public EmailNotVerifiedException(String email) {
		super(ErrorCode.EMAIL_NOT_VERIFIED, String.format("이메일 인증이 완료되지 않았습니다: %s", email));
	}
}
