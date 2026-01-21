package com.swcampus.domain.member.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class AdminNotFoundException extends BusinessException {

	public AdminNotFoundException() {
		super(ErrorCode.ADMIN_NOT_FOUND);
	}
}
