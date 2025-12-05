package com.swcampus.domain.teacher;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Teacher {
	private Long teacherId;
	private String teacherName;
	private String teacherDescription;
	private String teacherImageUrl;

	// 강사 정보 수정 등의 비즈니스 로직은 여기에 위치
}