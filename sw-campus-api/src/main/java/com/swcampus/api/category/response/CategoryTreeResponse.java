package com.swcampus.api.category.response;

import java.util.ArrayList;
import java.util.List;

import com.swcampus.domain.category.Category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "카테고리 트리 응답")
public class CategoryTreeResponse {

	@Schema(description = "카테고리 ID", example = "1")
	private Long categoryId;

	@Schema(description = "카테고리명", example = "프로그래밍")
	private String categoryName;

	@Schema(description = "정렬 순서", example = "1")
	private Integer sort;

	@Schema(description = "하위 카테고리 목록")
	private List<CategoryTreeResponse> children = new ArrayList<>(); // 자식 카테고리

	public CategoryTreeResponse(Category category) {
		this.categoryId = category.getCategoryId();
		this.categoryName = category.getCategoryName();
		this.sort = category.getSort();
		this.children = category.getChildren().stream()
			.map(CategoryTreeResponse::new)
			.toList();
	}

	// 자식 추가
	public void addChild(CategoryTreeResponse child) {
		this.children.add(child);
		// 자식이 추가될 때마다 정렬 순서(sort)대로 정렬 
		// this.children.sort(Comparator.comparing(CategoryTreeResponse::getSort));
	}
}