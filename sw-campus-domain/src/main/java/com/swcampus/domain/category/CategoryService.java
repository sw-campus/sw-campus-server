package com.swcampus.domain.category;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}
}
