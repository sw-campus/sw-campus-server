package com.swcampus.infra.postgres.category;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CurriculumJpaRepository extends JpaRepository<CurriculumEntity, Long> {
	List<CurriculumEntity> findByCategory_CategoryId(Long categoryId);
}