package com.swcampus.domain.storage.exception;

public class StorageBatchLimitExceededException extends RuntimeException {

    private static final int DEFAULT_LIMIT = 50;

    public StorageBatchLimitExceededException() {
        super("최대 " + DEFAULT_LIMIT + "개까지 요청 가능합니다");
    }

    public StorageBatchLimitExceededException(int limit) {
        super("최대 " + limit + "개까지 요청 가능합니다");
    }
}
