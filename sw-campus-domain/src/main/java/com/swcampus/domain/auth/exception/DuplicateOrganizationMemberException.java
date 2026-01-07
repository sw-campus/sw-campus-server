package com.swcampus.domain.auth.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class DuplicateOrganizationMemberException extends BusinessException {

	public DuplicateOrganizationMemberException() {
		super(ErrorCode.DUPLICATE_ORGANIZATION_MEMBER);
	}

	public DuplicateOrganizationMemberException(Long organizationId) {
		super(ErrorCode.DUPLICATE_ORGANIZATION_MEMBER,
				String.format("이미 다른 사용자가 연결된 기관입니다: %d", organizationId));
	}
}
