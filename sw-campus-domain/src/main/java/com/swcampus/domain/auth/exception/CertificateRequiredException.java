package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CertificateRequiredException extends BusinessException {

	public CertificateRequiredException() {
		super(ErrorCode.CERTIFICATE_REQUIRED);
	}
}
