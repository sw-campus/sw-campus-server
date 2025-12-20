package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

public class StorageAccessDeniedException extends DomainException {
    public static final String CODE = "스토리지 권한이 없습니다.";

    public StorageAccessDeniedException(String message) {
        super(CODE, message);
    }
}
