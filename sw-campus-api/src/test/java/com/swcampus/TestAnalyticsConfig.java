package com.swcampus;

import com.swcampus.domain.analytics.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.List;

/**
 * 테스트 환경용 Analytics Stub 설정
 */
@Configuration
@Profile("test")
public class TestAnalyticsConfig {

    @Bean
    @ConditionalOnMissingBean(AnalyticsRepository.class)
    public AnalyticsRepository stubAnalyticsRepository() {
        return new AnalyticsRepository() {
            @Override
            public AnalyticsReport getReport(int daysAgo) {
                return new AnalyticsReport(0, 0, 0, 0, Collections.emptyList());
            }

            @Override
            public EventStats getEventStats(int daysAgo) {
                return new EventStats(0, 0, 0, 0, 0, 0, Collections.emptyList());
            }

            @Override
            public List<BannerClickStats> getTopBannersByClicks(int daysAgo, int limit) {
                return Collections.emptyList();
            }

            @Override
            public List<LectureClickStats> getTopLecturesByClicks(int daysAgo, int limit) {
                return Collections.emptyList();
            }
        };
    }
}
