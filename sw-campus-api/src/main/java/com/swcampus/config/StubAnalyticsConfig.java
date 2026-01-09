package com.swcampus.config;

import com.swcampus.domain.analytics.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Google Analytics credentials가 설정되지 않은 환경에서 사용되는 Stub 설정
 * GoogleAnalyticsRepository가 생성되지 않을 때 fallback으로 사용됨
 */
@Configuration
public class StubAnalyticsConfig {

    @Bean
    @ConditionalOnMissingBean(AnalyticsRepository.class)
    public AnalyticsRepository stubAnalyticsRepository() {
        return new AnalyticsRepository() {
            @Override
            public AnalyticsReport getReport(int daysAgo) {
                return new AnalyticsReport(0, 0, 0, 0, 0, 0, Collections.emptyList(), Collections.emptyList());
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

            @Override
            public List<PopularLecture> getPopularLectures(int daysAgo, int limit) {
                return Collections.emptyList();
            }

            @Override
            public List<PopularSearchTerm> getPopularSearchTerms(int daysAgo, int limit) {
                return Collections.emptyList();
            }

            @Override
            public List<TrafficSource> getTrafficSources(int daysAgo, int limit) {
                return Collections.emptyList();
            }
        };
    }
}
