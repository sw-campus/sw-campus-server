package com.swcampus.domain.category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

	Optional<Category> findById(Long id);

	List<Category> findAll();

	// 상위 카테고리만 조회하거나, 특정 깊이의 카테고리 조회 등
	List<Category> findByPid(Long pid);
}