package com.swcampus.storage.postgres.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnProperty(prefix = "sw-campus-server.db", name = "write", havingValue = "postgres")
public class PostgresDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "sw-campus-server.datasource.write")
	public HikariConfig writeHikariConfig() {
		return new HikariConfig();
	}

	@Bean
	public HikariDataSource writeDataSource(@Qualifier("writeHikariConfig") HikariConfig config) {
		return new HikariDataSource(config);
	}
}
