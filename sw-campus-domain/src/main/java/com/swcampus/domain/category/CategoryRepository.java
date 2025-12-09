package com.swcampus.domain.category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

	Optional<Category> findById(Long id);

	List<Category> findAll();

	List<Category> findByPid(Long pid);
}