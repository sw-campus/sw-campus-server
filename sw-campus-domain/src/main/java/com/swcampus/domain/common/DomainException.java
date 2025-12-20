package com.swcampus.domain.common;

import java.util.Map;

/**
 * Base class for domain-level exceptions. Lives in the domain module to avoid
 * coupling API/infra concerns. API layer should translate this to HTTP responses.
 */
public class DomainException extends RuntimeException {
    private final String code;
    private final Map<String, Object> details;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public DomainException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
