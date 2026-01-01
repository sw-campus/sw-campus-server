package com.swcampus.domain.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Curriculum {
	private Long curriculumId;
	private Long categoryId;
	private String curriculumName;
	private String curriculumDesc;
	private Category category;
}