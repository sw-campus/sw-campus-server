package com.swcampus.infra.postgres.teacher;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swcampus.domain.teacher.Teacher;
import com.swcampus.domain.teacher.TeacherRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TeacherEntityRepository implements TeacherRepository {

	private final TeacherJpaRepository jpaRepository;

	@Override
	public Teacher save(Teacher teacher) {
		TeacherEntity entity = TeacherEntity.builder()
				.teacherId(teacher.getTeacherId())
				.teacherName(teacher.getTeacherName())
				.teacherDescription(teacher.getTeacherDescription())
				.teacherImageUrl(teacher.getTeacherImageUrl())
				.build();

		return jpaRepository.save(entity).toDomain();
	}

	@Override
	public Optional<Teacher> findById(Long id) {
		return jpaRepository.findById(id).map(TeacherEntity::toDomain);
	}
}