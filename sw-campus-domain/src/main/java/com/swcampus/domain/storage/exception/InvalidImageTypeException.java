package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

import java.util.Map;

public class InvalidImageTypeException extends DomainException {
    public static final String CODE = "IMAGE_TYPE_NOT_ALLOWED";

    public InvalidImageTypeException(String message) {
        super(CODE, message);
    }

    public InvalidImageTypeException(String message, Map<String, Object> details) {
        super(CODE, message, details);
    }
}
