package com.swcampus.domain.review.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class ReviewNotModifiableException extends BusinessException {

	public ReviewNotModifiableException() {
		super(ErrorCode.REVIEW_NOT_MODIFIABLE);
	}

	public ReviewNotModifiableException(Long reviewId) {
		super(ErrorCode.REVIEW_NOT_MODIFIABLE,
				String.format("승인된 후기는 수정할 수 없습니다. Review ID: %d", reviewId));
	}
}
