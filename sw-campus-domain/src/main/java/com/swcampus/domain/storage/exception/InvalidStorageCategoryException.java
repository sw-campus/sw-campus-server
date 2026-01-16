package com.swcampus.domain.storage.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class InvalidStorageCategoryException extends BusinessException {

	public InvalidStorageCategoryException(String category) {
		super(ErrorCode.INVALID_STORAGE_CATEGORY, "지원하지 않는 카테고리입니다: " + category);
	}
}
