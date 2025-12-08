package com.swcampus.infra.postgres.lecture;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "COHORTS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lecture")
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

	@Column(name = "TOTAL_DAYS", nullable = false)
	private Integer totalDays;

	public com.swcampus.domain.lecture.Cohort toDomain() {
		return com.swcampus.domain.lecture.Cohort.builder()
			.cohortNum(cohortNum)
			.lectureId(lecture.getLectureId())
			.startAt(startAt)
			.endAt(endAt)
			.totalDays(totalDays)
			.build();
	}
}