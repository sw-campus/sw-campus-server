package com.swcampus.domain.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public List<Category> getCategoryTree() {
		List<Category> allCategories = categoryRepository.findAll();

		Map<Long, Category> categoryMap = allCategories.stream()
			.collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

		List<Category> roots = new ArrayList<>();

		for (Category category : allCategories) {
			if (category.getPid() == null || category.getPid() == 0) {
				roots.add(category);
			} else {
				Category parent = categoryMap.get(category.getPid());
				if (parent != null) {
					parent.getChildren().add(category);
					// 자식 정렬
					// parent.getChildren().sort(Comparator.comparing(Category::getSort));
				}
			}
		}

		// Root 정렬
		// roots.sort(Comparator.comparing(Category::getSort));

		return roots;
	}

	@Transactional(readOnly = true)
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}
}
