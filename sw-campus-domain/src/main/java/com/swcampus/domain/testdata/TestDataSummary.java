package com.swcampus.domain.testdata;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestDataSummary {
    private boolean exists;
    private String batchId;
    private Map<String, Long> counts;
    private long totalCount;
    private OffsetDateTime createdAt;

    public static TestDataSummary empty() {
        TestDataSummary summary = new TestDataSummary();
        summary.exists = false;
        summary.counts = new HashMap<>();
        summary.totalCount = 0;
        return summary;
    }

    public static TestDataSummary of(String batchId, Map<String, Long> counts, OffsetDateTime createdAt) {
        TestDataSummary summary = new TestDataSummary();
        summary.exists = true;
        summary.batchId = batchId;
        summary.counts = counts != null ? counts : new HashMap<>();
        summary.totalCount = summary.counts.values().stream().mapToLong(Long::longValue).sum();
        summary.createdAt = createdAt;
        return summary;
    }
}
