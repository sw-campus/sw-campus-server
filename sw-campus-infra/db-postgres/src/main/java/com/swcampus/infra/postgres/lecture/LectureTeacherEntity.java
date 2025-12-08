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
public class LectureTeacherEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LECTURE_ID", nullable = false)
	private LectureEntity lecture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TEACHER_ID", nullable = false)
	private TeacherEntity teacher;
}