package com.swcampus.api.exception;

/**
 * 파일 처리 중 발생하는 예외입니다.
 * <p>
 * Note: 이 예외는 인프라스트럭처 레벨(IO)의 문제를 래핑하기 위해 사용되며,
 * 비즈니스 로직 위반이 아니므로 BusinessException을 상속하지 않고 RuntimeException을 상속합니다.
 * 추후 공통 예외 처리 구조가 확립되면 리팩토링될 수 있습니다.
 * </p>
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
