package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class BasicSurveyRequiredException extends BusinessException {

	public BasicSurveyRequiredException() {
		super(ErrorCode.BASIC_SURVEY_REQUIRED);
	}

	public BasicSurveyRequiredException(Long memberId) {
		super(ErrorCode.BASIC_SURVEY_REQUIRED,
			String.format("기초 설문을 먼저 완료해야 합니다. memberId: %d", memberId));
	}
}
