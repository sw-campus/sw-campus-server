package com.swcampus.swcampusserver.storage.db.postgres.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.swcampus.swcampusserver.storage.db.postgres")
@EnableJpaRepositories(basePackages = "com.swcampus.swcampusserver.storage.db.postgres")
public class PostgresJpaConfig {
}
