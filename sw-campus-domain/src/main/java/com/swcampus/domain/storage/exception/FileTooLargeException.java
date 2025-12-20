package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

import java.util.Map;

public class FileTooLargeException extends DomainException {
    public static final String CODE = "파일 용량이 초과되었습니다";

    public FileTooLargeException(String message) {
        super(CODE, message);
    }

    public FileTooLargeException(String message, Map<String, Object> details) {
        super(CODE, message, details);
    }
}
