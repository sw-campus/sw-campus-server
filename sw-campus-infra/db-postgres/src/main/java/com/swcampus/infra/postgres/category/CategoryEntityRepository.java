package com.swcampus.infra.postgres.category;

import com.swcampus.domain.category.Category;
import com.swcampus.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryEntityRepository implements CategoryRepository {

	private final CategoryJpaRepository jpaRepository;

	@Override
	public Optional<Category> findById(Long id) {
		return jpaRepository.findById(id).map(CategoryEntity::toDomain);
	}

	@Override
	public List<Category> findAll() {
		return jpaRepository.findAll().stream()
				.map(CategoryEntity::toDomain)
				.toList();
	}

	@Override
	public List<Category> findByPid(Long pid) {
		return jpaRepository.findByPid(pid).stream()
				.map(CategoryEntity::toDomain)
				.toList();
	}
}