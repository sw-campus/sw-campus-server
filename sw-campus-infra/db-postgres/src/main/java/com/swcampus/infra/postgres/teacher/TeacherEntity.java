package com.swcampus.infra.postgres.teacher;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TEACHERS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TeacherEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TEACHER_ID")
	private Long teacherId;

	@Column(name = "TEACHER_NAME", nullable = false, length = 50)
	private String teacherName;

	@Column(name = "TEACHER_DESCRIPTION")
	private String teacherDescription;

	@Column(name = "TEACHER_IMAGE_URL")
	private String teacherImageUrl;

	public com.swcampus.domain.teacher.Teacher toDomain() {
		return com.swcampus.domain.teacher.Teacher.builder()
				.teacherId(this.teacherId)
				.teacherName(this.teacherName)
				.teacherDescription(this.teacherDescription)
				.teacherImageUrl(this.teacherImageUrl)
				.build();
	}

	public static TeacherEntity from(com.swcampus.domain.teacher.Teacher teacher) {
		return TeacherEntity.builder()
				.teacherId(teacher.getTeacherId())
				.teacherName(teacher.getTeacherName())
				.teacherDescription(teacher.getTeacherDescription())
				.teacherImageUrl(teacher.getTeacherImageUrl())
				.build();
	}
}