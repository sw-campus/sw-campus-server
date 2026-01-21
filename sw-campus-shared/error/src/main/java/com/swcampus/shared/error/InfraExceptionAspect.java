package com.swcampus.shared.error;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인프라 계층 예외를 위한 Cross-cutting translator
 *
 * 모든 공용 메서드를 com.swcampus.infra...* 패키지로 감싸고,
 * 사용 가능한 {@link InfraExceptionTranslator} 빈을 사용하여
 * SDK/IO/네트워크 예외를 도메인 수준의 예외로 변환
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class InfraExceptionAspect {

	private final List<InfraExceptionTranslator> translators;

	@Around("execution(public * com.swcampus.infra..*(..))")
	public Object translateInfraExceptions(ProceedingJoinPoint pjp) throws Throwable {
		try {
			return pjp.proceed();
		} catch (Throwable t) {
			for (InfraExceptionTranslator tr : translators) {
				RuntimeException mapped = tr.translate(t);
				if (mapped != null)
					throw mapped;
			}
			throw t;
		}
	}
}
