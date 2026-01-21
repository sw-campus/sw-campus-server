package com.swcampus.infra.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(name = "certificate.ocr.enabled", havingValue = "true")
public class OcrConfig {

    @Bean("ocrRestTemplate")
    public RestTemplate ocrRestTemplate() {
        return new RestTemplate();
    }
}
