package com.swcampus.domain.category;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Category {
	private Long categoryId;
	private Long pid;
	private String categoryName;
	private Integer sort;
	
	@Builder.Default
	private List<Category> children = new ArrayList<>();
}