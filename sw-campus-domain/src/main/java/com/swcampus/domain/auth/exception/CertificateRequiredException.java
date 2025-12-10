package com.swcampus.domain.auth.exception;

public class CertificateRequiredException extends RuntimeException {
    
    public CertificateRequiredException() {
        super("재직증명서는 필수입니다");
    }
}
