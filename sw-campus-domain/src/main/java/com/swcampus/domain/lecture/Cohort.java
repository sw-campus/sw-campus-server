package com.swcampus.domain.lecture;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Cohort {
	private Long cohortNum;
	private Long lectureId;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
}