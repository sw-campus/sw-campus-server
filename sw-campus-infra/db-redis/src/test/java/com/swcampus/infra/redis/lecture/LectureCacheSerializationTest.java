package com.swcampus.infra.redis.lecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.swcampus.domain.category.Category;
import com.swcampus.domain.category.Curriculum;
import com.swcampus.domain.lecture.CurriculumLevel;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureAdd;
import com.swcampus.domain.lecture.LectureAuthStatus;
import com.swcampus.domain.lecture.LectureCurriculum;
import com.swcampus.domain.lecture.LectureDay;
import com.swcampus.domain.lecture.LectureLocation;
import com.swcampus.domain.lecture.LectureQual;
import com.swcampus.domain.lecture.LectureQualType;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.lecture.RecruitType;
import com.swcampus.domain.lecture.SelectionStepType;
import com.swcampus.domain.teacher.Teacher;

/**
 * Redis 캐시에서 사용하는 Jackson 직렬화/역직렬화 테스트
 * RedisConfig와 동일한 ObjectMapper 설정 사용
 */
class LectureCacheSerializationTest {

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		// RedisConfig와 동일한 설정
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.activateDefaultTyping(
				BasicPolymorphicTypeValidator.builder()
						.allowIfBaseType(Object.class)
						.build(),
				ObjectMapper.DefaultTyping.NON_FINAL);
	}

	@Test
	@DisplayName("Lecture 객체 직렬화/역직렬화 성공")
	void serializeAndDeserializeLecture() throws Exception {
		// given
		Lecture lecture = createFullLecture();

		// when
		String json = objectMapper.writeValueAsString(lecture);
		Lecture deserialized = objectMapper.readValue(json, Lecture.class);

		// then
		assertThat(deserialized).isNotNull();
		assertThat(deserialized.getLectureId()).isEqualTo(lecture.getLectureId());
		assertThat(deserialized.getLectureName()).isEqualTo(lecture.getLectureName());
		assertThat(deserialized.getDays()).containsExactlyInAnyOrderElementsOf(lecture.getDays());
		assertThat(deserialized.getStartTime()).isEqualTo(lecture.getStartTime());
		assertThat(deserialized.getStatus()).isEqualTo(lecture.getStatus());
	}

	@Test
	@DisplayName("Lecture의 중첩 객체(LectureStep) 역직렬화 성공")
	void deserializeLectureWithSteps() throws Exception {
		// given
		Lecture lecture = createFullLecture();

		// when
		String json = objectMapper.writeValueAsString(lecture);
		Lecture deserialized = objectMapper.readValue(json, Lecture.class);

		// then
		assertThat(deserialized.getSteps()).hasSize(1);
		assertThat(deserialized.getSteps().get(0).getStepType()).isEqualTo(SelectionStepType.DOCUMENT);
	}

	@Test
	@DisplayName("Lecture의 중첩 객체(Teacher) 역직렬화 성공")
	void deserializeLectureWithTeachers() throws Exception {
		// given
		Lecture lecture = createFullLecture();

		// when
		String json = objectMapper.writeValueAsString(lecture);
		Lecture deserialized = objectMapper.readValue(json, Lecture.class);

		// then
		assertThat(deserialized.getTeachers()).hasSize(1);
		assertThat(deserialized.getTeachers().get(0).getTeacherName()).isEqualTo("홍길동");
	}

	@Test
	@DisplayName("Lecture의 중첩 객체(LectureCurriculum -> Curriculum -> Category) 역직렬화 성공")
	void deserializeLectureWithCurriculum() throws Exception {
		// given
		Lecture lecture = createFullLecture();

		// when
		String json = objectMapper.writeValueAsString(lecture);
		Lecture deserialized = objectMapper.readValue(json, Lecture.class);

		// then
		assertThat(deserialized.getLectureCurriculums()).hasSize(1);
		LectureCurriculum lc = deserialized.getLectureCurriculums().get(0);
		assertThat(lc.getCurriculum()).isNotNull();
		assertThat(lc.getCurriculum().getCurriculumName()).isEqualTo("Spring Boot");
		assertThat(lc.getCurriculum().getCategory()).isNotNull();
		assertThat(lc.getCurriculum().getCategory().getCategoryName()).isEqualTo("백엔드");
	}

	private Lecture createFullLecture() {
		Category category = Category.builder()
				.categoryId(1L)
				.categoryName("백엔드")
				.build();

		Curriculum curriculum = Curriculum.builder()
				.curriculumId(1L)
				.categoryId(1L)
				.curriculumName("Spring Boot")
				.category(category)
				.build();

		LectureCurriculum lectureCurriculum = LectureCurriculum.builder()
				.lectureId(19L)
				.curriculumId(1L)
				.level(CurriculumLevel.BASIC)
				.curriculum(curriculum)
				.build();

		Teacher teacher = Teacher.builder()
				.teacherId(1L)
				.teacherName("홍길동")
				.teacherDescription("10년차 개발자")
				.build();

		LectureStep step = LectureStep.builder()
				.stepId(1L)
				.lectureId(19L)
				.stepType(SelectionStepType.DOCUMENT)
				.stepOrder(1)
				.build();

		LectureAdd add = LectureAdd.builder()
				.addId(1L)
				.lectureId(19L)
				.addName("노트북 지원")
				.build();

		LectureQual qual = LectureQual.builder()
				.qualId(1L)
				.lectureId(19L)
				.type(LectureQualType.REQUIRED)
				.text("프로그래밍 기초 지식")
				.build();

		return Lecture.builder()
				.lectureId(19L)
				.orgId(1L)
				.orgName("테스트 기관")
				.lectureName("Spring Boot 마스터")
				.days(Set.of(LectureDay.MONDAY, LectureDay.WEDNESDAY, LectureDay.FRIDAY))
				.startTime(LocalTime.of(9, 0))
				.endTime(LocalTime.of(18, 0))
				.lectureLoc(LectureLocation.OFFLINE)
				.location("서울시 강남구")
				.recruitType(RecruitType.CARD_REQUIRED)
				.subsidy(BigDecimal.valueOf(1000000))
				.lectureFee(BigDecimal.valueOf(5000000))
				.status(LectureStatus.RECRUITING)
				.lectureAuthStatus(LectureAuthStatus.APPROVED)
				.startAt(LocalDateTime.of(2025, 3, 1, 0, 0))
				.endAt(LocalDateTime.of(2025, 6, 30, 0, 0))
				.deadline(LocalDateTime.of(2025, 2, 28, 23, 59))
				.steps(List.of(step))
				.adds(List.of(add))
				.quals(List.of(qual))
				.teachers(List.of(teacher))
				.lectureCurriculums(List.of(lectureCurriculum))
				.build();
	}
}
