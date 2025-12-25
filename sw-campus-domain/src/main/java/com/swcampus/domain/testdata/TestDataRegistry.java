package com.swcampus.domain.testdata;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestDataRegistry {
    private Long id;
    private String batchId;
    private String tableName;
    private Long recordId;
    private OffsetDateTime createdAt;

    public static TestDataRegistry create(String batchId, String tableName, Long recordId) {
        TestDataRegistry registry = new TestDataRegistry();
        registry.batchId = batchId;
        registry.tableName = tableName;
        registry.recordId = recordId;
        registry.createdAt = OffsetDateTime.now();
        return registry;
    }

    public static TestDataRegistry of(Long id, String batchId, String tableName,
                                       Long recordId, OffsetDateTime createdAt) {
        TestDataRegistry registry = new TestDataRegistry();
        registry.id = id;
        registry.batchId = batchId;
        registry.tableName = tableName;
        registry.recordId = recordId;
        registry.createdAt = createdAt;
        return registry;
    }
}
