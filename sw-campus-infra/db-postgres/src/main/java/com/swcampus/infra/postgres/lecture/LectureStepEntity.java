package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.SelectionStepType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "LECTURE_STEPS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lecture")
public class LectureStepEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STEP_ID")
	private Long stepId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID")
	private LectureEntity lecture;

	@Column(name = "STEP_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private SelectionStepType stepType;

	@Column(name = "STEP_ORDER", nullable = false)
	private Integer stepOrder;

	@CreationTimestamp
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	public com.swcampus.domain.lecture.LectureStep toDomain() {
		return toDomain(null);
	}

	public com.swcampus.domain.lecture.LectureStep toDomain(Long fallbackLectureId) {
		Long resolvedLectureId = (lecture != null) ? lecture.getLectureId() : fallbackLectureId;

		return com.swcampus.domain.lecture.LectureStep.builder()
				.stepId(stepId)
				.lectureId(resolvedLectureId)
				.stepType(stepType)
				.stepOrder(stepOrder)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
}