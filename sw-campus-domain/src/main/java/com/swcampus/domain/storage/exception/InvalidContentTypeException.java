package com.swcampus.domain.storage.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class InvalidContentTypeException extends BusinessException {

	public InvalidContentTypeException(String contentType) {
		super(ErrorCode.INVALID_CONTENT_TYPE, "지원하지 않는 파일 형식입니다: " + contentType);
	}
}
