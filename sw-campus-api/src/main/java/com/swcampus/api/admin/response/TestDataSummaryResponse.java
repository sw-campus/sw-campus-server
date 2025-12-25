package com.swcampus.api.admin.response;

import com.swcampus.domain.testdata.TestDataSummary;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public record TestDataSummaryResponse(
        boolean exists,
        String batchId,
        Map<String, Long> counts,
        long totalCount,
        String createdAt
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static TestDataSummaryResponse from(TestDataSummary summary) {
        return new TestDataSummaryResponse(
                summary.isExists(),
                summary.getBatchId(),
                summary.getCounts(),
                summary.getTotalCount(),
                summary.getCreatedAt() != null ? summary.getCreatedAt().format(FORMATTER) : null
        );
    }
}
