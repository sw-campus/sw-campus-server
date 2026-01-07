package com.swcampus.domain.storage.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class StorageAccessDeniedException extends BusinessException {

	public StorageAccessDeniedException() {
		super(ErrorCode.STORAGE_ACCESS_DENIED);
	}
}
