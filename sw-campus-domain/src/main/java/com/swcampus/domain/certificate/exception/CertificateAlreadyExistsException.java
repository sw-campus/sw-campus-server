package com.swcampus.domain.certificate.exception;

public class CertificateAlreadyExistsException extends RuntimeException {

    public CertificateAlreadyExistsException() {
        super("이미 인증된 수료증입니다");
    }

    public CertificateAlreadyExistsException(Long memberId, Long lectureId) {
        super(String.format("이미 인증된 수료증입니다. memberId: %d, lectureId: %d", memberId, lectureId));
    }
}
