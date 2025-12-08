package com.swcampus.infra.postgres.lecture;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.infra.postgres.category.CurriculumEntity;
import com.swcampus.infra.postgres.teacher.TeacherEntity;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LectureEntityRepository implements LectureRepository {

	private final LectureJpaRepository jpaRepository;
	private final EntityManager entityManager;

	@Override
	public Lecture save(Lecture lecture) {
		LectureEntity entity = LectureEntity.from(lecture);

		// N:M Relationships (Teachers)
		if (lecture.getTeachers() != null) {
			lecture.getTeachers().forEach(t -> {
				TeacherEntity teacherRef;
				if (t.getTeacherId() != null) {
					teacherRef = entityManager.getReference(TeacherEntity.class, t.getTeacherId());
				} else {
					teacherRef = TeacherEntity.from(t);
					entityManager.persist(teacherRef);
				}
				entity.getLectureTeachers().add(LectureTeacherEntity.builder()
						.lecture(entity)
						.teacher(teacherRef)
						.build());
			});
		}

		// N:M Relationships (Curriculums)
		if (lecture.getLectureCurriculums() != null) {
			lecture.getLectureCurriculums().forEach(lc -> {
				CurriculumEntity curriculumRef = entityManager.getReference(CurriculumEntity.class, lc.getCurriculumId());
				entity.getLectureCurriculums().add(LectureCurriculumEntity.builder()
						.lecture(entity)
						.curriculum(curriculumRef)
						.level(lc.getLevel())
						.build());
			});
		}

		return jpaRepository.save(entity).toDomain();
	}

	@Override
	public Optional<Lecture> findById(Long id) {
		return jpaRepository.findById(id).map(LectureEntity::toDomain);
	}
}