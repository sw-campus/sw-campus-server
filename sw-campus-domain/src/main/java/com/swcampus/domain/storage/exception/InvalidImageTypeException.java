package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

import java.util.Map;

public class InvalidImageTypeException extends DomainException {
    public static final String CODE = "허용되지 않는 형식입니다";

    public InvalidImageTypeException(String message) {
        super(CODE, message);
    }

    public InvalidImageTypeException(String message, Map<String, Object> details) {
        super(CODE, message, details);
    }
}
