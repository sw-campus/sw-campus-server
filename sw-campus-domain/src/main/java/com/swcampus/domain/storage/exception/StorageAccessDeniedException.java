package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

public class StorageAccessDeniedException extends DomainException {
    public static final String CODE = "STORAGE_ACCESS_DENIED";

    public StorageAccessDeniedException(String message) {
        super(CODE, message);
    }
}
