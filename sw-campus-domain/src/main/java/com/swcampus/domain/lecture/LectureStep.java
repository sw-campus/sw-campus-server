package com.swcampus.domain.lecture;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class LectureStep {
	private Long stepId;
	private Long lectureId;
	private SelectionStepType stepType; // Enum으로 변경
	private Integer stepOrder;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}