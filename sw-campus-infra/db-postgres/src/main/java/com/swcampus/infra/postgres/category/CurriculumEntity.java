package com.swcampus.infra.postgres.category;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CURRICULUMS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CURRICULUM_ID")
	private Long curriculumId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CATEGORY_ID", nullable = false)
	private CategoryEntity category;

	@Column(name = "CURRICULUM_NAME")
	private String curriculumName;

	public com.swcampus.domain.category.Curriculum toDomain() {
		return com.swcampus.domain.category.Curriculum.builder()
				.curriculumId(this.curriculumId)
				.categoryId(this.category.getCategoryId())
				.curriculumName(this.curriculumName)
				.category(this.category.toDomain())
				.build();
	}
}