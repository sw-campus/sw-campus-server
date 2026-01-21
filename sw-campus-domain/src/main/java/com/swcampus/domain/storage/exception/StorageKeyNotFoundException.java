package com.swcampus.domain.storage.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class StorageKeyNotFoundException extends BusinessException {

	public StorageKeyNotFoundException(String key) {
		super(ErrorCode.STORAGE_KEY_NOT_FOUND, "존재하지 않는 파일입니다: " + key);
	}
}
