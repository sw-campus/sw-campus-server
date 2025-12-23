package com.swcampus.api.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rate Limiting을 적용할 API에 사용하는 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Rate Limit 키 (같은 키를 가진 API는 카운트를 공유)
     */
    String key();

    /**
     * 윈도우 시간 내 최대 요청 횟수
     */
    int limit() default 20;

    /**
     * 윈도우 시간 (초 단위)
     */
    int windowSeconds() default 60;
}
