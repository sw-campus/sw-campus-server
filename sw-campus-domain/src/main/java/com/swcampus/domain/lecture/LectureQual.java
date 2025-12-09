package com.swcampus.domain.lecture;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureQual {
	private Long qualId;
	private Long lectureId;
	private LectureQualType type; // REQUIRED/PREFERRED
	private String text;
}