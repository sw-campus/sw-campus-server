package com.swcampus.infra.postgres.testdata;

import com.swcampus.domain.testdata.TestDataRegistry;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "test_data_registry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestDataRegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, length = 50)
    private String batchId;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static TestDataRegistryEntity from(TestDataRegistry registry) {
        TestDataRegistryEntity entity = new TestDataRegistryEntity();
        entity.id = registry.getId();
        entity.batchId = registry.getBatchId();
        entity.tableName = registry.getTableName();
        entity.recordId = registry.getRecordId();
        entity.createdAt = registry.getCreatedAt() != null ? registry.getCreatedAt() : OffsetDateTime.now();
        return entity;
    }

    public TestDataRegistry toDomain() {
        return TestDataRegistry.of(id, batchId, tableName, recordId, createdAt);
    }
}
