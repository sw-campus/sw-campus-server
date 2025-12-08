package com.swcampus.domain.lecture;

import com.swcampus.domain.category.Curriculum;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureCurriculum {
	private Long lectureId;
	private Long curriculumId;
	private CurriculumLevel level; // 없음/기본/심화

	private Curriculum curriculum;
}