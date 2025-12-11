package com.swcampus.infra.postgres.lecture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.infra.postgres.category.CurriculumEntity;
import com.swcampus.infra.postgres.lecture.mapper.LectureMapper;
import com.swcampus.infra.postgres.teacher.TeacherEntity;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LectureEntityRepository implements LectureRepository {

	private final LectureJpaRepository jpaRepository;
	private final EntityManager entityManager;
	private final LectureMapper lectureMapper;

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

	@Override
	public Page<Lecture> searchLectures(LectureSearchCondition condition) {
		List<Lecture> content = lectureMapper.selectLectures(condition).stream()
				.map(LectureEntity::toDomain)
				.toList();

		long total = lectureMapper.countLectures(condition);
		
		int page = (condition.getLimit() != null && condition.getLimit() > 0
			&& condition.getOffset() != null)
			? (int) (condition.getOffset() / condition.getLimit())
			: 0;
		int size = (condition.getLimit() != null && condition.getLimit() > 0)
			? condition.getLimit()
			: content.size() + 1; // avoid /0

		return new PageImpl<>(content, PageRequest.of(page, size), total);
	}

	@Override
	public List<Lecture> findAllExpiredAndRecruiting(LocalDateTime now) {
		return jpaRepository.findAllByDeadlineBeforeAndStatus(now, LectureStatus.RECRUITING)
				.stream()
				.map(LectureEntity::toDomain)
				.toList();
	}
}