package com.swcampus.domain.category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Curriculum {
	private Long curriculumId;
	private Long categoryId;
	private String curriculumName;
	private String curriculumDesc;
	private Category category;
}