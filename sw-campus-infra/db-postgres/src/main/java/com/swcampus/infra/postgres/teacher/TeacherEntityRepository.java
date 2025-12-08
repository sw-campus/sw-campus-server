package com.swcampus.infra.postgres.teacher;

import com.swcampus.domain.teacher.Teacher;
import com.swcampus.domain.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

	@Override
	public Optional<Teacher> findByName(String name) {
		return jpaRepository.findByTeacherName(name).map(TeacherEntity::toDomain);
	}

	@Override
	public void deleteById(Long id) {
		jpaRepository.deleteById(id);
	}
}