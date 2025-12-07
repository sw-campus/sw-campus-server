package com.swcampus.domain.lecture;

import com.swcampus.domain.category.Curriculum;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureCurriculum {
	private Long lectureId;
	private Long curriculumId;
	private String level; // 입문/기본/심화

	// 상세 정보를 위해 포함
	private Curriculum curriculum;
}