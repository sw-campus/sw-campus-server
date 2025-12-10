package com.swcampus.domain.category;

import java.util.List;
import java.util.Optional;

public interface CurriculumRepository {

	Optional<Curriculum> findById(Long id);

	List<Curriculum> findAll();

	List<Curriculum> findByCategoryId(Long categoryId);
}