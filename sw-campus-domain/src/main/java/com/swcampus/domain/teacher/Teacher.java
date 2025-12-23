package com.swcampus.domain.teacher;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Teacher {
	private Long teacherId;
	private String teacherName;
	private String teacherDescription;
	private String teacherImageUrl;
}