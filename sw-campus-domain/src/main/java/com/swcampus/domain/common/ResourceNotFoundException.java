package com.swcampus.domain.common;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

	public ResourceNotFoundException(String message) {
		super(ErrorCode.RESOURCE_NOT_FOUND, message);
	}
}
