package com.swcampus.api.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.swcampus.api.category.response.CategoryTreeResponse;
import com.swcampus.domain.category.Category;
import com.swcampus.domain.category.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	/**
	 * 카테고리 전체 트리 조회 API
	 * 프론트엔드 대/중/소 Select Box 구성용
	 */
	@GetMapping("/tree")
	public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
		List<Category> categoryTree = categoryService.getCategoryTree();
		List<CategoryTreeResponse> response = categoryTree.stream()
			.map(CategoryTreeResponse::new)
			.toList();

		return ResponseEntity.ok(response);
	}
}
