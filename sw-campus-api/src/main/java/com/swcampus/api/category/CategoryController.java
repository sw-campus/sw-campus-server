package com.swcampus.api.category;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swcampus.api.category.response.CategoryTreeResponse;
import com.swcampus.domain.category.Category;
import com.swcampus.domain.category.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관리 API")
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping("/tree")
	@Operation(summary = "카테고리 트리 조회", description = "대/중/소 카테고리 전체 트리를 조회합니다. 프론트엔드 Select Box 구성용입니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
		List<Category> categoryTree = categoryService.getCategoryTree();
		List<CategoryTreeResponse> response = categoryTree.stream()
			.map(CategoryTreeResponse::new)
			.toList();

		return ResponseEntity.ok(response);
	}
}
