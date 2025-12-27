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
import com.swcampus.domain.lecture.LectureAuthStatus;
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
	private final com.swcampus.domain.review.ReviewRepository reviewRepository;

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

	@Override
	public Lecture updateAuthStatus(Long lectureId, LectureAuthStatus status) {
		LectureEntity entity = jpaRepository.findById(lectureId)
				.orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
		entity.updateAuthStatus(status);
		return jpaRepository.save(entity).toDomain();
	}

	private void updateEntityFields(LectureEntity entity, Lecture lecture) {
		entity.updateFields(lecture);
	}

	private void updateCollections(LectureEntity entity, Lecture lecture) {
		// 1:N Steps
		entity.getSteps().clear();
		// 1:N Adds
		entity.getAdds().clear();
		// 1:N Quals
		entity.getQuals().clear();
		// N:M Teachers
		entity.getLectureTeachers().clear();
		// N:M Curriculums
		entity.getLectureCurriculums().clear();
		// Force flush to execute deletes before inserts (prevents PK/UK violation)
		entityManager.flush();

		// Now add new Step entities
		if (lecture.getSteps() != null) {
			entity.getSteps().addAll(lecture.getSteps().stream()
					.map(s -> LectureStepEntity.builder()
							.lecture(entity)
							.stepType(s.getStepType())
							.stepOrder(s.getStepOrder())
							.build())
					.toList());
		}

		// Now add new Add entities
		if (lecture.getAdds() != null) {
			entity.getAdds().addAll(lecture.getAdds().stream()
					.map(a -> LectureAddEntity.builder()
							.lecture(entity)
							.addName(a.getAddName())
							.build())
					.toList());
		}

		// Now add new Qual entities
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
		// 1. Lecture + 연관 엔티티 조회 (FETCH JOIN)
		Optional<LectureEntity> entityOpt = jpaRepository.findByIdWithCategory(id);
		if (entityOpt.isEmpty()) {
			return Optional.empty();
		}

		LectureEntity entity = entityOpt.get();

		// 2. 리뷰 통계 별도 조회 (GROUP BY 충돌 방지)
		List<Object[]> reviewStatsList = jpaRepository.findReviewStatsByLectureId(id);
		Object[] reviewStats = reviewStatsList.isEmpty() ? null : reviewStatsList.get(0);
		Double avgScore = reviewStats != null && reviewStats[0] != null ? ((Number) reviewStats[0]).doubleValue() : 0.0;
		Long reviewCount = reviewStats != null && reviewStats[1] != null ? ((Number) reviewStats[1]).longValue() : 0L;

		return Optional.of(entity.toDomain().toBuilder()
				.averageScore(avgScore != null ? avgScore : 0.0)
				.reviewCount(reviewCount != null ? reviewCount : 0L)
				.build());
	}

	@Override
	public Map<Long, String> findLectureNamesByIds(List<Long> lectureIds) {
		if (lectureIds == null || lectureIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<Object[]> results = jpaRepository.findIdAndNameByIdIn(lectureIds);
		return results.stream()
				.collect(java.util.stream.Collectors.toMap(
						row -> (Long) row[0],
						row -> (String) row[1]));
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
		return jpaRepository.findAllByOrgIdWithCurriculums(orgId)
				.stream()
				.map(LectureEntity::toDomain)
				.toList();
	}

	@Override
	public List<Lecture> findAllByOrgIdAndLectureAuthStatus(Long orgId, LectureAuthStatus status) {
		return jpaRepository.findAllByOrgIdAndLectureAuthStatusWithCurriculums(orgId, status)
				.stream()
				.map(LectureEntity::toDomain)
				.toList();
	}

	@Override
	public Map<Long, Long> countLecturesByStatusAndAuthStatusAndOrgIdIn(LectureStatus status,
			LectureAuthStatus authStatus, List<Long> orgIds) {
		if (orgIds == null || orgIds.isEmpty()) {
			return Map.of();
		}
		List<Object[]> results = jpaRepository.countByStatusAndOrgIdInGroupByOrgId(status, authStatus, orgIds);
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
		List<Lecture> lectures = jpaRepository.findAllByIdInWithCurriculums(lectureIds).stream()
				.map(LectureEntity::toDomain)
				.toList();

		if (lectures.isEmpty()) {
			return lectures;
		}

		// 2 쿼리 → 1 쿼리 최적화: 평균 점수와 리뷰 수를 한 번에 조회
		Map<Long, Map<String, Number>> reviewStats = reviewRepository.getReviewStatsByLectureIds(lectureIds);

		return lectures.stream()
				.map(l -> {
					Map<String, Number> stats = reviewStats.getOrDefault(l.getLectureId(), Map.of());
					Double avg = stats.getOrDefault("avgScore", 0.0).doubleValue();
					Long count = stats.getOrDefault("reviewCount", 0L).longValue();
					return l.toBuilder()
							.averageScore(avg)
							.reviewCount(count)
							.build();
				})
				.toList();
	}

	/**
	 * 리뷰 통계 없이 강의 목록 조회 (장바구니 등 리뷰 불필요한 경우)
	 * findAllByIds() 대비 1회 쿼리 감소
	 */
	@Override
	public List<Lecture> findAllByIdsWithoutReviewStats(List<Long> lectureIds) {
		if (lectureIds == null || lectureIds.isEmpty()) {
			return Collections.emptyList();
		}
		return jpaRepository.findAllByIdInWithCurriculums(lectureIds).stream()
				.map(LectureEntity::toDomain)
				.toList();
	}

	@Override
	public long countAll() {
		return jpaRepository.count();
	}

	@Override
	public long countByAuthStatus(LectureAuthStatus status) {
		return jpaRepository.countByLectureAuthStatus(status);
	}

	@Override
	public void deleteById(Long id) {
		jpaRepository.deleteById(id);
	}
}