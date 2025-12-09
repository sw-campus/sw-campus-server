package com.swcampus.infra.postgres.category;

import com.swcampus.domain.category.Curriculum;
import com.swcampus.domain.category.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CurriculumEntityRepository implements CurriculumRepository {

	private final CurriculumJpaRepository jpaRepository;

	@Override
	public Optional<Curriculum> findById(Long id) {
		return jpaRepository.findById(id).map(CurriculumEntity::toDomain);
	}

	@Override
	public List<Curriculum> findAll() {
		return jpaRepository.findAll().stream()
				.map(CurriculumEntity::toDomain)
				.toList();
	}

	@Override
	public List<Curriculum> findByCategoryId(Long categoryId) {
		return jpaRepository.findByCategory_CategoryId(categoryId).stream()
				.map(CurriculumEntity::toDomain)
				.toList();
	}
}