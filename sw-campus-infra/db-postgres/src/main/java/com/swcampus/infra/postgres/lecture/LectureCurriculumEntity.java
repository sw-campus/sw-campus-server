package com.swcampus.infra.postgres.lecture;

import com.swcampus.infra.postgres.category.CurriculumEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LECTURE_CURRICULUMS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"lecture", "curriculum"})
public class LectureCurriculumEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lecture_curriculums_seq")
	@SequenceGenerator(name = "lecture_curriculums_seq", sequenceName = "lecture_curriculums_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID", nullable = false)
	private LectureEntity lecture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CURRICULUM_ID", nullable = false)
	private CurriculumEntity curriculum;

	@Column(name = "LEVEL")
	@Enumerated(EnumType.STRING)
	private com.swcampus.domain.lecture.CurriculumLevel level; // 없음/기본/심화

	public com.swcampus.domain.lecture.LectureCurriculum toDomain() {
		return com.swcampus.domain.lecture.LectureCurriculum.builder()
				.lectureId(lecture.getLectureId())
				.curriculumId(curriculum.getCurriculumId())
				.level(level)
				.curriculum(curriculum.toDomain())
				.build();
	}
}