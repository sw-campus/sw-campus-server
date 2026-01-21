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
public class LectureSpecialCurriculum {
	private Long specialCurriculumId;
	private Long lectureId;
	private String title;
	private Integer sortOrder;
}
