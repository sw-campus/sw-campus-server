package com.swcampus.domain.category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Category {
	private Long categoryId;
	private Long pid;
	private String categoryName;
	private Integer sort;
}