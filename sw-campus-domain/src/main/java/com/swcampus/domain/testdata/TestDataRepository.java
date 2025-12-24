package com.swcampus.domain.testdata;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestDataRepository {

    TestDataRegistry save(TestDataRegistry registry);

    List<TestDataRegistry> saveAll(List<TestDataRegistry> registries);

    boolean exists();

    Optional<String> findLatestBatchId();

    List<TestDataRegistry> findByTableName(String tableName);

    Map<String, Long> countByTable();

    void deleteAll();
}
