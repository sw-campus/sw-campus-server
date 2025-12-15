package com.swcampus.api.lecture;

import static com.swcampus.domain.lecture.EquipmentType.*;
import static com.swcampus.domain.lecture.LectureLocation.*;
import static com.swcampus.domain.lecture.RecruitType.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.lecture.SelectionStepType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.api.lecture.request.LectureCreateRequest;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;

@WebMvcTest(controllers = LectureController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("LectureController - 강의 테스트")
class LectureControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private LectureService lectureService;

        @MockitoBean
        private OrganizationService organizationService;

        @MockitoBean
        private TokenProvider tokenProvider;

        @BeforeEach
        void setUp() {
                UsernamePasswordAuthenticationToken authentication = mock(
                                UsernamePasswordAuthenticationToken.class);
                when(authentication.getDetails()).thenReturn(1L); // userId
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("강의 생성 성공")
        @Disabled("Skipped by user request: Security Context dependency")
        void createLecture() throws Exception {
                // given
                // LectureCreateRequest의 모든 필드를 null로 설정하되 필수값만 채움
                LectureCreateRequest request = new LectureCreateRequest(
                                1L, "Java Spring 강의", null, null, null, OFFLINE, null, CARD_REQUIRED,
                                null, null, null, null, 100, NONE, null, false, false, false, false, null, null, null,
                                null, null, null, false, "2024-01-01", "2024-12-31", null, null, null,
                                null, null, null,
                                java.util.List.of(new LectureCreateRequest.TeacherRequest(null, "New Teacher", "Desc")),
                                null);

                Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
                // Reflection to set ID for mock
                org.springframework.test.util.ReflectionTestUtils.setField(organization, "id", 10L);

                when(organizationService.getOrganizationByUserId(1L)).thenReturn(organization);

                Lecture lecture = Lecture.builder()
                                .lectureId(100L)
                                .lectureName("Java Spring 강의")
                                .status(LectureStatus.RECRUITING)
                                .build();

                // Service method now takes 5 args: lecture, imageBytes, imageName, contentType,
                // teacherImages
                when(lectureService.registerLecture(any(Lecture.class), any(), any(), any(), anyList()))
                                .thenReturn(lecture);

                // MockMultipartFile for the JSON part
                org.springframework.mock.web.MockMultipartFile lecturePart = new org.springframework.mock.web.MockMultipartFile(
                                "lecture",
                                "",
                                "application/json",
                                objectMapper.writeValueAsBytes(request));

                // MockMultipartFile for teacher image
                org.springframework.mock.web.MockMultipartFile teacherImage = new org.springframework.mock.web.MockMultipartFile(
                                "teacherImages",
                                "teacher.jpg",
                                "image/jpeg",
                                "image content".getBytes());

                // when & then
                mockMvc.perform(multipart("/api/v1/lectures")
                                .file(lecturePart)
                                .file(teacherImage)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.lecture_id").value(100))
                                .andExpect(jsonPath("$.lecture_name").value("Java Spring 강의"));
        }

        @Test
        @DisplayName("강의 상세 조회 성공")
        void getLecture() throws Exception {
                // given
                Lecture lecture = Lecture.builder()
                                .lectureId(100L)
                                .lectureName("Test Lecture")
                                .status(LectureStatus.RECRUITING)
                                .build();
                when(lectureService.getPublishedLecture(100L)).thenReturn(lecture);

                // when & then
                mockMvc.perform(get("/api/v1/lectures/{lectureId}", 100L)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lecture_id").value(100))
                                .andExpect(jsonPath("$.lecture_name").value("Test Lecture"));
        }

        @Test
        @DisplayName("강의 검색 성공")
        void searchLectures() throws Exception {
                // given
                LectureStep step = LectureStep.builder()
                                .stepId(1L)
                                .stepType(SelectionStepType.CODING_TEST)
                                .stepOrder(1)
                                .build();

                Lecture lecture = Lecture.builder()
                                .lectureId(100L)
                                .lectureName("Search Result")
                                .status(LectureStatus.RECRUITING)
                                .steps(List.of(step))
                                .build();
                Page<Lecture> page = new PageImpl<>(List.of(lecture), Pageable.unpaged(), 1);

                when(lectureService.searchLectures(any(LectureSearchCondition.class))).thenReturn(page);

                // when & then
                mockMvc.perform(get("/api/v1/lectures/search")
                                .with(csrf())
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].lecture_id").value(100))
                                .andExpect(jsonPath("$.content[0].lecture_name").value("Search Result"))
                                .andExpect(jsonPath("$.content[0].steps[0].step_type").value("CODING_TEST"));
        }
}
