package com.swcampus.infra.postgres.testdata;

import com.swcampus.domain.testdata.TestDataRegistry;
import com.swcampus.domain.testdata.TestDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TestDataRegistryEntityRepository implements TestDataRepository {

    private final TestDataRegistryJpaRepository jpaRepository;

    @Override
    public TestDataRegistry save(TestDataRegistry registry) {
        TestDataRegistryEntity entity = TestDataRegistryEntity.from(registry);
        TestDataRegistryEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<TestDataRegistry> saveAll(List<TestDataRegistry> registries) {
        List<TestDataRegistryEntity> entities = registries.stream()
                .map(TestDataRegistryEntity::from)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(TestDataRegistryEntity::toDomain)
                .toList();
    }

    @Override
    public boolean exists() {
        return jpaRepository.count() > 0;
    }

    @Override
    public Optional<String> findLatestBatchId() {
        return jpaRepository.findLatestBatchId();
    }

    @Override
    public List<TestDataRegistry> findByTableName(String tableName) {
        return jpaRepository.findByTableName(tableName).stream()
                .map(TestDataRegistryEntity::toDomain)
                .toList();
    }

    @Override
    public Map<String, Long> countByTable() {
        Map<String, Long> counts = new HashMap<>();
        List<String> tableNames = jpaRepository.findDistinctTableNames();
        for (String tableName : tableNames) {
            counts.put(tableName, jpaRepository.countByTableName(tableName));
        }
        return counts;
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
