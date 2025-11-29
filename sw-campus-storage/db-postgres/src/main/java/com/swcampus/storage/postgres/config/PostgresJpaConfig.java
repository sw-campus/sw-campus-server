package com.swcampus.storage.postgres.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.swcampus.storage.db.postgres")
@EnableJpaRepositories(basePackages = "com.swcampus.storage.postgres")
public class PostgresJpaConfig {
}
