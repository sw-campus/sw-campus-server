package com.swcampus.domain.certificate.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CertificateNotFoundException extends BusinessException {

	public CertificateNotFoundException() {
		super(ErrorCode.CERTIFICATE_NOT_FOUND);
	}

	public CertificateNotFoundException(Long id) {
		super(ErrorCode.CERTIFICATE_NOT_FOUND, String.format("수료증을 찾을 수 없습니다. ID: %d", id));
	}
}
