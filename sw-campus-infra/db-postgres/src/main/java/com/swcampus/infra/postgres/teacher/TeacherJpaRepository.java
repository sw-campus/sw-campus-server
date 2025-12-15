package com.swcampus.infra.postgres.teacher;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherJpaRepository extends JpaRepository<TeacherEntity, Long> {
    List<TeacherEntity> findAllByTeacherNameContaining(String teacherName);
}