package com.swcampus.domain.review.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class ReviewNotFoundException extends BusinessException {

	public ReviewNotFoundException() {
		super(ErrorCode.REVIEW_NOT_FOUND);
	}

	public ReviewNotFoundException(Long id) {
		super(ErrorCode.REVIEW_NOT_FOUND, String.format("후기를 찾을 수 없습니다. ID: %d", id));
	}
}
