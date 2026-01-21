package com.swcampus.domain.certificate.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class CertificateLectureMismatchException extends BusinessException {

	public CertificateLectureMismatchException() {
		super(ErrorCode.CERTIFICATE_LECTURE_MISMATCH);
	}
}
