package com.swcampus.domain.lecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LectureSpecialCurriculumTest {

	@Nested
	@DisplayName("LectureSpecialCurriculum 생성 테스트")
	class CreateTest {

		@Test
		@DisplayName("빌더로 특화 커리큘럼 생성 성공")
		void create_withBuilder_success() {
			// given & when
			LectureSpecialCurriculum curriculum = LectureSpecialCurriculum.builder()
					.specialCurriculumId(1L)
					.lectureId(100L)
					.title("실무 프로젝트")
					.sortOrder(1)
					.build();

			// then
			assertThat(curriculum.getSpecialCurriculumId()).isEqualTo(1L);
			assertThat(curriculum.getLectureId()).isEqualTo(100L);
			assertThat(curriculum.getTitle()).isEqualTo("실무 프로젝트");
			assertThat(curriculum.getSortOrder()).isEqualTo(1);
		}

		@Test
		@DisplayName("정렬 순서 지정하여 특화 커리큘럼 생성 성공")
		void create_withSortOrder_success() {
			// given & when
			LectureSpecialCurriculum curriculum = LectureSpecialCurriculum.builder()
					.specialCurriculumId(1L)
					.lectureId(100L)
					.title("AI 특강")
					.sortOrder(2)
					.build();

			// then
			assertThat(curriculum.getTitle()).isEqualTo("AI 특강");
			assertThat(curriculum.getSortOrder()).isEqualTo(2);
		}

		@Test
		@DisplayName("ID 없이 새 특화 커리큘럼 생성 (저장 전)")
		void create_withoutId_forNewCurriculum() {
			// given & when
			LectureSpecialCurriculum curriculum = LectureSpecialCurriculum.builder()
					.lectureId(100L)
					.title("멘토링 프로그램")
					.sortOrder(3)
					.build();

			// then
			assertThat(curriculum.getSpecialCurriculumId()).isNull();
			assertThat(curriculum.getLectureId()).isEqualTo(100L);
			assertThat(curriculum.getTitle()).isEqualTo("멘토링 프로그램");
		}
	}

	@Nested
	@DisplayName("Lecture와 SpecialCurriculums 통합 테스트")
	class LectureIntegrationTest {

		@Test
		@DisplayName("Lecture에 특화 커리큘럼 목록 포함")
		void lecture_withSpecialCurriculums() {
			// given
			List<LectureSpecialCurriculum> specialCurriculums = List.of(
					LectureSpecialCurriculum.builder()
							.specialCurriculumId(1L)
							.lectureId(100L)
							.title("실무 프로젝트")
							.sortOrder(1)
							.build(),
					LectureSpecialCurriculum.builder()
							.specialCurriculumId(2L)
							.lectureId(100L)
							.title("취업 특강")
							.sortOrder(2)
							.build()
			);

			// when
			Lecture lecture = Lecture.builder()
					.lectureId(100L)
					.lectureName("Java 백엔드 부트캠프")
					.specialCurriculums(specialCurriculums)
					.build();

			// then
			assertThat(lecture.getSpecialCurriculums()).hasSize(2);
			assertThat(lecture.getSpecialCurriculums().get(0).getTitle()).isEqualTo("실무 프로젝트");
			assertThat(lecture.getSpecialCurriculums().get(1).getTitle()).isEqualTo("취업 특강");
		}

		@Test
		@DisplayName("Lecture에 특화 커리큘럼 없음")
		void lecture_withoutSpecialCurriculums() {
			// when
			Lecture lecture = Lecture.builder()
					.lectureId(100L)
					.lectureName("기본 강의")
					.specialCurriculums(null)
					.build();

			// then
			assertThat(lecture.getSpecialCurriculums()).isNull();
		}

		@Test
		@DisplayName("Lecture에 빈 특화 커리큘럼 목록")
		void lecture_withEmptySpecialCurriculums() {
			// when
			Lecture lecture = Lecture.builder()
					.lectureId(100L)
					.lectureName("기본 강의")
					.specialCurriculums(List.of())
					.build();

			// then
			assertThat(lecture.getSpecialCurriculums()).isEmpty();
		}

		@Test
		@DisplayName("특화 커리큘럼 최대 5개 시나리오")
		void lecture_withMaxFiveSpecialCurriculums() {
			// given
			List<LectureSpecialCurriculum> specialCurriculums = List.of(
					createCurriculum(1L, "실무 프로젝트", 1),
					createCurriculum(2L, "취업 특강", 2),
					createCurriculum(3L, "멘토링", 3),
					createCurriculum(4L, "네트워킹", 4),
					createCurriculum(5L, "포트폴리오 리뷰", 5)
			);

			// when
			Lecture lecture = Lecture.builder()
					.lectureId(100L)
					.lectureName("풀스택 부트캠프")
					.specialCurriculums(specialCurriculums)
					.build();

			// then
			assertThat(lecture.getSpecialCurriculums()).hasSize(5);
		}

		@Test
		@DisplayName("특화 커리큘럼 정렬 순서 확인")
		void lecture_specialCurriculums_sortOrder() {
			// given
			List<LectureSpecialCurriculum> specialCurriculums = List.of(
					createCurriculum(3L, "세 번째", 3),
					createCurriculum(1L, "첫 번째", 1),
					createCurriculum(2L, "두 번째", 2)
			);

			// when
			Lecture lecture = Lecture.builder()
					.lectureId(100L)
					.specialCurriculums(specialCurriculums)
					.build();

			// 정렬
			List<LectureSpecialCurriculum> sorted = lecture.getSpecialCurriculums().stream()
					.sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
					.toList();

			// then
			assertThat(sorted.get(0).getTitle()).isEqualTo("첫 번째");
			assertThat(sorted.get(1).getTitle()).isEqualTo("두 번째");
			assertThat(sorted.get(2).getTitle()).isEqualTo("세 번째");
		}

		private LectureSpecialCurriculum createCurriculum(Long id, String title, int sortOrder) {
			return LectureSpecialCurriculum.builder()
					.specialCurriculumId(id)
					.lectureId(100L)
					.title(title)
					.sortOrder(sortOrder)
					.build();
		}
	}
}
