package com.swcampus.domain.certificate.exception;

public class CertificateNotVerifiedException extends RuntimeException {

    public CertificateNotVerifiedException() {
        super("수료증 인증이 필요합니다");
    }

    public CertificateNotVerifiedException(Long lectureId) {
        super(String.format("해당 강의의 수료증 인증이 필요합니다. 강의 ID: %d", lectureId));
    }
}
