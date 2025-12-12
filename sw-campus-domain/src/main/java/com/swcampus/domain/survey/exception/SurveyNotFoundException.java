package com.swcampus.domain.survey.exception;

public class SurveyNotFoundException extends RuntimeException {

    public SurveyNotFoundException() {
        super("설문조사를 찾을 수 없습니다");
    }

    public SurveyNotFoundException(Long userId) {
        super(String.format("설문조사를 찾을 수 없습니다. userId: %d", userId));
    }
}
