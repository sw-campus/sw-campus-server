package com.swcampus.domain.lecture.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class LectureNotModifiableException extends BusinessException {

	public LectureNotModifiableException() {
		super(ErrorCode.LECTURE_NOT_MODIFIABLE);
	}
}
