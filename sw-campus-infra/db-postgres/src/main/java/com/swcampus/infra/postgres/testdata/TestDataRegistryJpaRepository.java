package com.swcampus.infra.postgres.testdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TestDataRegistryJpaRepository extends JpaRepository<TestDataRegistryEntity, Long> {

    List<TestDataRegistryEntity> findByTableName(String tableName);

    @Query("SELECT DISTINCT e.batchId FROM TestDataRegistryEntity e ORDER BY e.batchId DESC")
    Optional<String> findLatestBatchId();

    @Query("SELECT DISTINCT e.tableName FROM TestDataRegistryEntity e")
    List<String> findDistinctTableNames();

    long countByTableName(String tableName);

    void deleteByTableNameAndRecordIdIn(String tableName, List<Long> recordIds);
}
