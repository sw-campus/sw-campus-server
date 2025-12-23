package com.swcampus.domain.lecture;

import com.swcampus.domain.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @InjectMocks
    private LectureService lectureService;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Nested
    @DisplayName("강의 수정")
    class ModifyLectureTest {

        @Test
        @DisplayName("반려된 강의 수정 성공 및 상태 변경(PENDING)")
        void modifyLecture_rejected_success() {
            // given
            Long lectureId = 1L;
            Long orgId = 1L;
            Lecture existingLecture = Lecture.builder()
                    .lectureId(lectureId)
                    .orgId(orgId)
                    .lectureAuthStatus(LectureAuthStatus.REJECTED)
                    .build();

            Lecture updateParams = Lecture.builder()
                    .lectureName("수정된 강의")
                    .build();

            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(existingLecture));
            given(lectureRepository.save(any(Lecture.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Lecture result = lectureService.modifyLecture(lectureId, orgId, updateParams, null, null, null, Collections.emptyList());

            // then
            assertThat(result.getLectureAuthStatus()).isEqualTo(LectureAuthStatus.PENDING);
        }

        @Test
        @DisplayName("승인된 강의 수정 성공 및 상태 유지(APPROVED)")
        void modifyLecture_approved_success() {
            // given
            Long lectureId = 1L;
            Long orgId = 1L;
            Lecture existingLecture = Lecture.builder()
                    .lectureId(lectureId)
                    .orgId(orgId)
                    .lectureAuthStatus(LectureAuthStatus.APPROVED)
                    .build();

            Lecture updateParams = Lecture.builder()
                    .lectureName("수정된 강의")
                    .build();

            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(existingLecture));
            given(lectureRepository.save(any(Lecture.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Lecture result = lectureService.modifyLecture(lectureId, orgId, updateParams, null, null, null, Collections.emptyList());

            // then
            assertThat(result.getLectureAuthStatus()).isEqualTo(LectureAuthStatus.APPROVED);
        }

        @Test
        @DisplayName("대기중인 강의 수정 성공 및 상태 유지(PENDING)")
        void modifyLecture_pending_success() {
            // given
            Long lectureId = 1L;
            Long orgId = 1L;
            Lecture existingLecture = Lecture.builder()
                    .lectureId(lectureId)
                    .orgId(orgId)
                    .lectureAuthStatus(LectureAuthStatus.PENDING)
                    .build();

            Lecture updateParams = Lecture.builder()
                    .lectureName("수정된 강의")
                    .build();

            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.of(existingLecture));
            given(lectureRepository.save(any(Lecture.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Lecture result = lectureService.modifyLecture(lectureId, orgId, updateParams, null, null, null, Collections.emptyList());

            // then
            assertThat(result.getLectureAuthStatus()).isEqualTo(LectureAuthStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 강의 수정 시 예외 발생")
        void modifyLecture_notFound_throwsException() {
            // given
            Long lectureId = 999L;
            Long orgId = 1L;

            given(lectureRepository.findById(lectureId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> lectureService.modifyLecture(lectureId, orgId, Lecture.builder().build(), null, null, null, Collections.emptyList()))
                    .isInstanceOf(com.swcampus.domain.common.ResourceNotFoundException.class);
        }
    }
}
