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
		List<Category> allCategories = categoryService.getAllCategories();

		// DTO로 변환하면서 Map에 저장 (Key: ID, Value: DTO)
		// ID로 부모를 O(1)로 바로 찾을 수 있음
		Map<Long, CategoryTreeResponse> dtoMap = allCategories.stream()
			.map(CategoryTreeResponse::new)
			.collect(Collectors.toMap(CategoryTreeResponse::getCategoryId, Function.identity()));

		List<CategoryTreeResponse> roots = new ArrayList<>();

		for (Category cat : allCategories) {
			CategoryTreeResponse currentDto = dtoMap.get(cat.getCategoryId());
			Long pid = cat.getPid();

			if (pid == null || pid == 0) {
				// 부모가 없으면 최상위(대분류) -> Root 리스트에 추가
				roots.add(currentDto);
			} else {
				// 부모가 있으면 -> 부모 DTO를 찾아서 그 자식으로 추가
				CategoryTreeResponse parentDto = dtoMap.get(pid);
				if (parentDto != null) {
					parentDto.addChild(currentDto);
				}
			}
		}

		// 정렬 (Sort 순서) 
		// roots.sort((a, b) -> a.getSort().compareTo(b.getSort()));

		return ResponseEntity.ok(roots);
	}
}
