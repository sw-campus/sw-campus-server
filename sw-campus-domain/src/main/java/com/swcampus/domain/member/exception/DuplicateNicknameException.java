package com.swcampus.domain.member.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class DuplicateNicknameException extends BusinessException {

	public DuplicateNicknameException() {
		super(ErrorCode.DUPLICATE_NICKNAME);
	}

	public DuplicateNicknameException(String nickname) {
		super(ErrorCode.DUPLICATE_NICKNAME, String.format("이미 사용 중인 닉네임입니다: %s", nickname));
	}
}
