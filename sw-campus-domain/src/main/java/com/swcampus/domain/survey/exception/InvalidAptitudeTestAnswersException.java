package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class InvalidAptitudeTestAnswersException extends BusinessException {

	public InvalidAptitudeTestAnswersException(String message) {
		super(ErrorCode.INVALID_APTITUDE_TEST_ANSWERS, message);
	}
}
