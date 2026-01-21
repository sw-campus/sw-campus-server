package com.swcampus.shared.error;

/**
 * 인프라/클라이언트 예외(SDK, IO, 네트워크)를 상위 계층이 이해할 수 있는 도메인 수준의 예외로 변환.
 *
 * 구현:
 * Throwable을 처리하는 방법을 알고 있다면 매핑된 RuntimeException을 반환.
 * Throwable이 인식되지 않으면 null로 반환.
 */
public interface InfraExceptionTranslator {
    /**
     * @param t original throwable from infra/client layer
     * @return mapped RuntimeException (domain-oriented), or null if not handled
     */
    RuntimeException translate(Throwable t);
}
