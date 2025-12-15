package com.swcampus.domain.survey.exception;

public class SurveyAlreadyExistsException extends RuntimeException {

    public SurveyAlreadyExistsException() {
        super("이미 설문조사를 작성하셨습니다");
    }

    public SurveyAlreadyExistsException(Long memberId) {
        super(String.format("이미 설문조사를 작성하셨습니다. memberId: %d", memberId));
    }
}
