package com.swcampus.domain.category;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Category {
	private Long categoryId;
	private Long pid;
	private String categoryName;
	private Integer sort;
	
	@Builder.Default
	private List<Category> children = new ArrayList<>();
}