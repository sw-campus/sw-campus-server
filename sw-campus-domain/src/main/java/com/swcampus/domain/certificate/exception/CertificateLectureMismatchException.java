package com.swcampus.domain.certificate.exception;

public class CertificateLectureMismatchException extends RuntimeException {

    public CertificateLectureMismatchException() {
        super("해당 강의의 수료증이 아닙니다");
    }
}
