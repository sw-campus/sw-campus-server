package com.swcampus.domain.survey.exception;

import com.swcampus.shared.error.BusinessException;
import com.swcampus.shared.error.ErrorCode;

public class QuestionSetNotEditableException extends BusinessException {

	public QuestionSetNotEditableException() {
		super(ErrorCode.QUESTION_SET_NOT_EDITABLE);
	}

	public QuestionSetNotEditableException(Long questionSetId) {
		super(ErrorCode.QUESTION_SET_NOT_EDITABLE,
			String.format("발행된 문항 세트는 수정/삭제할 수 없습니다: questionSetId=%d", questionSetId));
	}
}
