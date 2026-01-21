package com.swcampus.domain.review.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class ReviewAlreadyExistsException extends BusinessException {

	public ReviewAlreadyExistsException() {
		super(ErrorCode.REVIEW_ALREADY_EXISTS);
	}

	public ReviewAlreadyExistsException(Long memberId, Long lectureId) {
		super(ErrorCode.REVIEW_ALREADY_EXISTS,
				String.format("이미 후기를 작성한 강의입니다. memberId: %d, lectureId: %d", memberId, lectureId));
	}
}
