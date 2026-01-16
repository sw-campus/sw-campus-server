package com.swcampus.domain.certificate.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CertificateAlreadyExistsException extends BusinessException {

	public CertificateAlreadyExistsException() {
		super(ErrorCode.CERTIFICATE_ALREADY_EXISTS);
	}

	public CertificateAlreadyExistsException(Long memberId, Long lectureId) {
		super(ErrorCode.CERTIFICATE_ALREADY_EXISTS,
				String.format("이미 인증된 수료증입니다. memberId: %d, lectureId: %d", memberId, lectureId));
	}
}
