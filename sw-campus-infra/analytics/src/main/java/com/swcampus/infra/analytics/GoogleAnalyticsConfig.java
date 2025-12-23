package com.swcampus.infra.analytics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;

/**
 * Google Analytics Data API 클라이언트 설정
 */
@Configuration
public class GoogleAnalyticsConfig {

    private final ResourceLoader resourceLoader;

    @Value("${google.analytics.credentials-path}")
    private String credentialsPath;

    @Value("${google.analytics.property-id}")
    private String propertyId;

    public GoogleAnalyticsConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public BetaAnalyticsDataClient betaAnalyticsDataClient() throws IOException {
        Resource resource = resourceLoader.getResource(credentialsPath);
        
        try (InputStream inputStream = resource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);

            BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

            return BetaAnalyticsDataClient.create(settings);
        }
    }

    @Bean
    public String googleAnalyticsPropertyId() {
        return propertyId;
    }
}
