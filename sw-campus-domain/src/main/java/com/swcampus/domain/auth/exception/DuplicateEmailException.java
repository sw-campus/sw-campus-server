package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class DuplicateEmailException extends BusinessException {

	public DuplicateEmailException() {
		super(ErrorCode.DUPLICATE_EMAIL);
	}

	public DuplicateEmailException(String email) {
		super(ErrorCode.DUPLICATE_EMAIL, String.format("이미 가입된 이메일입니다: %s", email));
	}
}
