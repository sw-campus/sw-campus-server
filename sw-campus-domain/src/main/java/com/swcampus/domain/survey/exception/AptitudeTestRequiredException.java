package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class AptitudeTestRequiredException extends BusinessException {

	public AptitudeTestRequiredException() {
		super(ErrorCode.APTITUDE_TEST_REQUIRED);
	}

	public AptitudeTestRequiredException(Long memberId) {
		super(ErrorCode.APTITUDE_TEST_REQUIRED,
			String.format("성향 테스트를 먼저 완료해야 합니다. memberId: %d", memberId));
	}
}
