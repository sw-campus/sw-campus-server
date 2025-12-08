package com.swcampus.infra.postgres.lecture;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.infra.postgres.category.CurriculumEntity;
import com.swcampus.infra.postgres.lecture.LectureCurriculumEntity;
import com.swcampus.infra.postgres.lecture.LectureTeacherEntity;
import com.swcampus.infra.postgres.teacher.TeacherEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LectureEntityRepository implements LectureRepository {

	private final LectureJpaRepository jpaRepository;
	private final EntityManager entityManager;

	@Override
	public Lecture save(Lecture lecture) {
		LectureEntity entity = LectureEntity.from(lecture);

		if (lecture.getTeachers() != null) {
			entity.getLectureTeachers().clear(); 
			lecture.getTeachers().forEach(t -> {
				TeacherEntity teacherRef = entityManager.getReference(TeacherEntity.class, t.getTeacherId());
				entity.getLectureTeachers().add(LectureTeacherEntity.builder()
						.lecture(entity)
						.teacher(teacherRef)
						.build());
			});
		}

		if (lecture.getLectureCurriculums() != null) {
			entity.getLectureCurriculums().clear(); 
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