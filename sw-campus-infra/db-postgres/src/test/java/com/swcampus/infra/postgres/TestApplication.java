package com.swcampus.infra.postgres;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * infra:db-postgres 모듈의 단위 테스트용 설정
 * @SpringBootApplication 대신 명시적 설정을 사용하여
 * 메인 애플리케이션 실행 시 빈 충돌 방지
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.swcampus.infra.postgres")
@EnableJpaRepositories(basePackages = "com.swcampus.infra.postgres")
public class TestApplication {
}
