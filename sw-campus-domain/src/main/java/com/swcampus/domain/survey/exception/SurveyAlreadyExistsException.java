package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class SurveyAlreadyExistsException extends BusinessException {

	public SurveyAlreadyExistsException() {
		super(ErrorCode.SURVEY_ALREADY_EXISTS);
	}

	public SurveyAlreadyExistsException(Long memberId) {
		super(ErrorCode.SURVEY_ALREADY_EXISTS,
				String.format("이미 설문조사를 작성하셨습니다. memberId: %d", memberId));
	}
}
