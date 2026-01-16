package com.swcampus.domain.certificate.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

/**
 * 수료증을 수정할 수 없는 경우 발생하는 예외
 * APPROVED 상태의 수료증은 수정 불가
 */
public class CertificateNotEditableException extends BusinessException {

	public CertificateNotEditableException(Long certificateId) {
		super(ErrorCode.CERTIFICATE_NOT_EDITABLE,
			String.format("승인된 수료증은 수정할 수 없습니다. ID: %d", certificateId));
	}
}
