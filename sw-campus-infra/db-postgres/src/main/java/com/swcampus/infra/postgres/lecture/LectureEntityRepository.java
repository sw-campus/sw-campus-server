package com.swcampus.infra.postgres.lecture;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureRepository;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.infra.postgres.category.CurriculumEntity;
import com.swcampus.infra.postgres.lecture.mapper.LectureMapper;
import com.swcampus.infra.postgres.teacher.TeacherEntity;

import jakarta.persistence.EntityManager;
import com.swcampus.domain.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LectureEntityRepository implements LectureRepository {

	private final LectureJpaRepository jpaRepository;
	private final EntityManager entityManager;
	private final LectureMapper lectureMapper;

	@Override
	public Lecture save(Lecture lecture) {
		LectureEntity entity;
		if (lecture.getLectureId() != null) {
			// Update: Fetch existing entity
			entity = jpaRepository.findById(lecture.getLectureId())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Lecture not found with id: " + lecture.getLectureId()));

			// Update scalar fields
			updateEntityFields(entity, lecture);

			// Update Collections (Clear and Add)
			updateCollections(entity, lecture);
		} else {
			// Create: New entity
			entity = LectureEntity.from(lecture);
			updateCollections(entity, lecture);
		}

		return jpaRepository.save(entity).toDomain();
	}

	private void updateEntityFields(LectureEntity entity, Lecture lecture) {
		entity.updateFields(lecture);
	}

	private void updateCollections(LectureEntity entity, Lecture lecture) {
		// 1:N Steps
		entity.getSteps().clear();
		if (lecture.getSteps() != null) {
			entity.getSteps().addAll(lecture.getSteps().stream()
					.map(s -> LectureStepEntity.builder()
							.lecture(entity)
							.stepType(s.getStepType())
							.stepOrder(s.getStepOrder())
							.build())
					.toList());
		}

		// 1:N Adds
		entity.getAdds().clear();
		if (lecture.getAdds() != null) {
			entity.getAdds().addAll(lecture.getAdds().stream()
					.map(a -> LectureAddEntity.builder()
							.lecture(entity)
							.addName(a.getAddName())
							.build())
					.toList());
		}

		// 1:N Quals
		entity.getQuals().clear();
		if (lecture.getQuals() != null) {
			entity.getQuals().addAll(lecture.getQuals().stream()
					.map(q -> LectureQualEntity.builder()
							.lecture(entity)
							.type(q.getType())
							.text(q.getText())
							.build())
					.toList());
		}

		// N:M Teachers
		entity.getLectureTeachers().clear();
		// Force flush to execute deletes before inserts (prevents UK violation)
		entityManager.flush();

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

		// N:M Curriculums
		entity.getLectureCurriculums().clear();
		// Force flush to execute deletes before inserts (prevents UK violation)
		entityManager.flush();

		if (lecture.getLectureCurriculums() != null) {
			lecture.getLectureCurriculums().forEach(lc -> {
				CurriculumEntity curriculumRef = entityManager.getReference(CurriculumEntity.class,
						lc.getCurriculumId());
				entity.getLectureCurriculums().add(LectureCurriculumEntity.builder()
						.lecture(entity)
						.curriculum(curriculumRef)
						.level(lc.getLevel())
						.build());
			});
		}
	}

	@Override
	public void saveAll(List<Lecture> lectures) {
		List<LectureEntity> entities = lectures.stream()
				.map(LectureEntity::from)
				.toList();
		jpaRepository.saveAll(entities);
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

		return new PageImpl<>(content, condition.getPageable(), total);
	}

	@Override
	public List<Lecture> findAllExpiredAndRecruiting(LocalDateTime now) {
		return jpaRepository.findAllByDeadlineBeforeAndStatus(now, LectureStatus.RECRUITING)
				.stream()
				.map(LectureEntity::toDomain)
				.toList();
	}

	@Override
	public List<Lecture> findAllByOrgId(Long orgId) {
		return jpaRepository.findAllByOrgId(orgId)
				.stream()
				.map(LectureEntity::toDomain)
				.toList();
	}

	@Override
	public Map<Long, Long> countLecturesByStatusAndOrgIdIn(LectureStatus status, List<Long> orgIds) {
		if (orgIds == null || orgIds.isEmpty()) {
			return Map.of();
		}
		List<Object[]> results = jpaRepository.countByStatusAndOrgIdInGroupByOrgId(status, orgIds);
		return results.stream()
				.collect(java.util.stream.Collectors.toMap(
						row -> (Long) row[0],
						row -> (Long) row[1]));
	}

	@Override
	public List<Lecture> findAllByIds(List<Long> lectureIds) {
		if (lectureIds == null || lectureIds.isEmpty()) {
			return Collections.emptyList();
		}
		// Full fetch to ensure relationships (like Curriculums -> Category) are loaded
		return jpaRepository.findAllById(lectureIds).stream()
				.map(LectureEntity::toDomain)
				.toList();
	}
}