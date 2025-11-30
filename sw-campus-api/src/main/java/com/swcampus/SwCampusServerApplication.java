package com.swcampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SwCampusServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwCampusServerApplication.class, args);
	}

}
