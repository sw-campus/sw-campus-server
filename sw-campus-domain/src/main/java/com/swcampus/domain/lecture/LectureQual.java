package com.swcampus.domain.lecture;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class LectureQual {
	private Long qualId;
	private Long lectureId;
	private LectureQualType type; // REQUIRED/PREFERRED
	private String text;
}