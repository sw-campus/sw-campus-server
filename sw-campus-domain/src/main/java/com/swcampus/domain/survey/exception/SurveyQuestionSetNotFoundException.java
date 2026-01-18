package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class SurveyQuestionSetNotFoundException extends BusinessException {

	public SurveyQuestionSetNotFoundException() {
		super(ErrorCode.SURVEY_QUESTION_SET_NOT_FOUND);
	}

	public SurveyQuestionSetNotFoundException(String type) {
		super(ErrorCode.SURVEY_QUESTION_SET_NOT_FOUND,
			String.format("발행된 %s 문항 세트를 찾을 수 없습니다.", type));
	}
}
