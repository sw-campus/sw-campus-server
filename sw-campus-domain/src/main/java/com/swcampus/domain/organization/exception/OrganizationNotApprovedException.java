package com.swcampus.domain.organization.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class OrganizationNotApprovedException extends BusinessException {

	public OrganizationNotApprovedException() {
		super(ErrorCode.ORGANIZATION_NOT_APPROVED);
	}

	public OrganizationNotApprovedException(String message) {
		super(ErrorCode.ORGANIZATION_NOT_APPROVED, message);
	}
}
