package com.swcampus.api.category.response;

import java.util.ArrayList;
import java.util.List;

import com.swcampus.domain.category.Category;

import lombok.Getter;

@Getter
public class CategoryTreeResponse {
	private Long categoryId;
	private String categoryName;
	private Integer sort;
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