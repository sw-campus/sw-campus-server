package com.swcampus.domain.lecture;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureAdd {
	private Long addId;
	private Long lectureId;
	private String addName;
}