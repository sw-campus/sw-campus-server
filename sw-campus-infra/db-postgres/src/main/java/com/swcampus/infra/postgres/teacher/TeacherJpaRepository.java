package com.swcampus.infra.postgres.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherJpaRepository extends JpaRepository<TeacherEntity, Long> {
    java.util.Optional<TeacherEntity> findByTeacherName(String teacherName);
}