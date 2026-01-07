package com.swcampus.domain.review.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class ReviewNotOwnerException extends BusinessException {

	public ReviewNotOwnerException() {
		super(ErrorCode.REVIEW_NOT_OWNER);
	}
}
