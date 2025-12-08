package com.swcampus.infra.postgres.category;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CATEGORIES")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CATEGORY_ID")
	private Long categoryId;

	// 계층 구조를 위한 부모 카테고리 ID
	@Column(name = "PID")
	private Long pid;

	@Column(name = "CATEGORY_NAME", nullable = false)
	private String categoryName;

	@Column(name = "SORT", nullable = false)
	private Integer sort;

	public com.swcampus.domain.category.Category toDomain() {
		return com.swcampus.domain.category.Category.builder()
				.categoryId(this.categoryId)
				.pid(this.pid)
				.categoryName(this.categoryName)
				.sort(this.sort)
				.build();
	}

	public static CategoryEntity from(com.swcampus.domain.category.Category category) {
		return CategoryEntity.builder()
				.categoryId(category.getCategoryId())
				.pid(category.getPid())
				.categoryName(category.getCategoryName())
				.sort(category.getSort())
				.build();
	}
}