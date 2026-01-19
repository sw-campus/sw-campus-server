package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class SurveyOptionNotFoundException extends BusinessException {

	public SurveyOptionNotFoundException() {
		super(ErrorCode.SURVEY_OPTION_NOT_FOUND);
	}

	public SurveyOptionNotFoundException(Long optionId) {
		super(ErrorCode.SURVEY_OPTION_NOT_FOUND,
			String.format("선택지를 찾을 수 없습니다: optionId=%d", optionId));
	}
}
