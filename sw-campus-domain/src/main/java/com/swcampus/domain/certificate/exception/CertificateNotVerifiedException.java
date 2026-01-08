package com.swcampus.domain.certificate.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CertificateNotVerifiedException extends BusinessException {

	public CertificateNotVerifiedException() {
		super(ErrorCode.CERTIFICATE_NOT_VERIFIED);
	}

	public CertificateNotVerifiedException(Long lectureId) {
		super(ErrorCode.CERTIFICATE_NOT_VERIFIED,
				String.format("해당 강의의 수료증 인증이 필요합니다. 강의 ID: %d", lectureId));
	}
}
