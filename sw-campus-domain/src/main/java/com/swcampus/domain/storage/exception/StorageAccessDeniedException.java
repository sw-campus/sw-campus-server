package com.swcampus.domain.storage.exception;

public class StorageAccessDeniedException extends RuntimeException {

    public StorageAccessDeniedException() {
        super("관리자만 접근 가능합니다");
    }
}
