package com.swcampus.infra.postgres.lecture;

import java.time.LocalDateTime;

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
@Table(name = "COHORTS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CohortEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "COHORT_NUM")
	private Long cohortNum;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID")
	private LectureEntity lecture;

	@Column(name = "START_AT", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "END_AT", nullable = false)
	private LocalDateTime endAt;

	public com.swcampus.domain.lecture.Cohort toDomain() {
		return com.swcampus.domain.lecture.Cohort.builder()
			.cohortNum(cohortNum)
			.lectureId(lecture.getLectureId())
			.startAt(startAt)
			.endAt(endAt)
			.build();
	}
}