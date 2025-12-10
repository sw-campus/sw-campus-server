package com.swcampus.domain.certificate.exception;

public class CertificateNotFoundException extends RuntimeException {

    public CertificateNotFoundException() {
        super("수료증을 찾을 수 없습니다");
    }

    public CertificateNotFoundException(Long id) {
        super(String.format("수료증을 찾을 수 없습니다. ID: %d", id));
    }
}
