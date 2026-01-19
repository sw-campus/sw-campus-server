package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class SurveyQuestionNotFoundException extends BusinessException {

	public SurveyQuestionNotFoundException() {
		super(ErrorCode.SURVEY_QUESTION_NOT_FOUND);
	}

	public SurveyQuestionNotFoundException(Long questionId) {
		super(ErrorCode.SURVEY_QUESTION_NOT_FOUND,
			String.format("문항을 찾을 수 없습니다: questionId=%d", questionId));
	}
}
