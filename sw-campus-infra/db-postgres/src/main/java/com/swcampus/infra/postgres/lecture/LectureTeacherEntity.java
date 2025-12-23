package com.swcampus.infra.postgres.lecture;

import com.swcampus.infra.postgres.teacher.TeacherEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LECTURE_TEACHERS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"lecture", "teacher"})
public class LectureTeacherEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lecture_teachers_seq")
	@SequenceGenerator(name = "lecture_teachers_seq", sequenceName = "lecture_teachers_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID", nullable = false)
	private LectureEntity lecture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TEACHER_ID", nullable = false)
	private TeacherEntity teacher;
}