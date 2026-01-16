package com.swcampus.domain.storage.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class StorageBatchLimitExceededException extends BusinessException {

	private static final int DEFAULT_LIMIT = 50;

	public StorageBatchLimitExceededException() {
		super(ErrorCode.STORAGE_BATCH_LIMIT_EXCEEDED, "최대 " + DEFAULT_LIMIT + "개까지 요청 가능합니다");
	}

	public StorageBatchLimitExceededException(int limit) {
		super(ErrorCode.STORAGE_BATCH_LIMIT_EXCEEDED, "최대 " + limit + "개까지 요청 가능합니다");
	}
}
