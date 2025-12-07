package com.swcampus.domain.lecture;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureStep {
	private Long stepId;
	private Long lectureId;
	private String stepName;
	private Integer stepOrder;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}