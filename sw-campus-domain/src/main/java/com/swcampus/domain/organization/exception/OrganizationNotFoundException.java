package com.swcampus.domain.organization.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class OrganizationNotFoundException extends BusinessException {

	public OrganizationNotFoundException() {
		super(ErrorCode.ORGANIZATION_NOT_FOUND);
	}

	public OrganizationNotFoundException(Long id) {
		super(ErrorCode.ORGANIZATION_NOT_FOUND, "기관을 찾을 수 없습니다. id: " + id);
	}
}
