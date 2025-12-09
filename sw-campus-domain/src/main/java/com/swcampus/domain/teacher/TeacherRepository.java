package com.swcampus.domain.teacher;

import java.util.Optional;

public interface TeacherRepository {
	Teacher save(Teacher teacher);

	Optional<Teacher> findById(Long id);
}