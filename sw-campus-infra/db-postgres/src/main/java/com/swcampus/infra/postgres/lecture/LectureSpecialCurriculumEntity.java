package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.LectureSpecialCurriculum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "LECTURE_SPECIAL_CURRICULUMS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lecture")
public class LectureSpecialCurriculumEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lecture_special_curriculums_seq")
	@SequenceGenerator(name = "lecture_special_curriculums_seq", sequenceName = "lecture_special_curriculums_special_curriculum_id_seq", allocationSize = 1)
	@Column(name = "SPECIAL_CURRICULUM_ID")
	private Long specialCurriculumId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID")
	private LectureEntity lecture;

	@Column(name = "TITLE", nullable = false, length = 20)
	private String title;

	@Column(name = "SORT_ORDER", nullable = false)
	private Integer sortOrder;

	public LectureSpecialCurriculum toDomain() {
		return LectureSpecialCurriculum.builder()
				.specialCurriculumId(specialCurriculumId)
				.lectureId(lecture.getLectureId())
				.title(title)
				.sortOrder(sortOrder)
				.build();
	}
}
