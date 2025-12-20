package com.swcampus.domain.storage.exception;

import com.swcampus.domain.common.DomainException;

import java.util.Map;

/**
 * Wraps infrastructure/storage layer failures (e.g., AWS S3 SDK errors, network timeouts).
 * This lives in the domain module so infra can throw it without depending on the API layer.
 * The API layer decides the final HTTP status, but a suggested status can be provided.
 */
public class StorageServiceException extends DomainException {
    public static final String CODE = "STORAGE_ERROR";

    private final Integer suggestedHttpStatus; // nullable; API may choose a default when null

    public StorageServiceException(String message) {
        super(CODE, message);
        this.suggestedHttpStatus = null;
    }

    public StorageServiceException(String message, Integer suggestedHttpStatus) {
        super(CODE, message);
        this.suggestedHttpStatus = suggestedHttpStatus;
    }

    public StorageServiceException(String message, Map<String, Object> details, Integer suggestedHttpStatus) {
        super(CODE, message, details);
        this.suggestedHttpStatus = suggestedHttpStatus;
    }

    public Integer getSuggestedHttpStatus() {
        return suggestedHttpStatus;
    }
}
