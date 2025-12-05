package com.swcampus.infra.postgres.lecture;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LECTURE_STEPS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureStepEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STEP_ID")
	private Long stepId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID")
	private LectureEntity lecture;

	@Column(name = "STEP_NAME", nullable = false)
	private String stepName;

	@Column(name = "STEP_ORDER", nullable = false)
	private Integer stepOrder;

	@CreationTimestamp
	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "UPDATED_AT")
	private LocalDateTime updatedAt;

	public com.swcampus.domain.lecture.LectureStep toDomain() {
		return com.swcampus.domain.lecture.LectureStep.builder()
			.stepId(stepId)
			.lectureId(lecture.getLectureId())
			.stepName(stepName)
			.stepOrder(stepOrder)
			.createdAt(createdAt)
			.updatedAt(updatedAt)
			.build();
	}
}