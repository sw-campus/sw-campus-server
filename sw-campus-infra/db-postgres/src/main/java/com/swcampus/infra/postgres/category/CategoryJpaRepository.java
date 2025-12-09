package com.swcampus.infra.postgres.category;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
	List<CategoryEntity> findByPid(Long pid);
}