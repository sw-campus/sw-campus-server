package com.swcampus.domain.teacher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Teacher {
	private Long teacherId;
	private String teacherName;
	private String teacherDescription;
	private String teacherImageUrl;
}