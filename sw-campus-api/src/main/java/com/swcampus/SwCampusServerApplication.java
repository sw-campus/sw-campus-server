package com.swcampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableScheduling
@SpringBootApplication
public class SwCampusServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwCampusServerApplication.class, args);
	}

}
