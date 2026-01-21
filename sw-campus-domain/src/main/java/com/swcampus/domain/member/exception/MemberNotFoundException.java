package com.swcampus.domain.member.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class MemberNotFoundException extends BusinessException {

	public MemberNotFoundException() {
		super(ErrorCode.MEMBER_NOT_FOUND);
	}

	public MemberNotFoundException(Long id) {
		super(ErrorCode.MEMBER_NOT_FOUND, String.format("회원을 찾을 수 없습니다. ID: %d", id));
	}

	public MemberNotFoundException(String message) {
		super(ErrorCode.MEMBER_NOT_FOUND, message);
	}
}
