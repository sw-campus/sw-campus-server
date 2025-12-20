package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

import java.util.Map;

public class FileTooLargeException extends DomainException {
    public static final String CODE = "FILE_TOO_LARGE";

    public FileTooLargeException(String message) {
        super(CODE, message);
    }

    public FileTooLargeException(String message, Map<String, Object> details) {
        super(CODE, message, details);
    }
}
