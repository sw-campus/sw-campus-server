package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class SurveyNotFoundException extends BusinessException {

	public SurveyNotFoundException() {
		super(ErrorCode.SURVEY_NOT_FOUND);
	}

	public SurveyNotFoundException(Long memberId) {
		super(ErrorCode.SURVEY_NOT_FOUND, String.format("설문조사를 찾을 수 없습니다. memberId: %d", memberId));
	}
}
