package com.swcampus.api.lecture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.lecture.LectureStep;
import com.swcampus.domain.lecture.SelectionStepType;
import com.swcampus.domain.lecture.dto.LectureSearchCondition;
import com.swcampus.domain.lecture.dto.LectureSummaryDto;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.organization.OrganizationService;
import com.swcampus.domain.review.ReviewService;

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

        @MockitoBean
        private ReviewService reviewService;

        @BeforeEach
        void setUp() {
                UsernamePasswordAuthenticationToken authentication = mock(
                                UsernamePasswordAuthenticationToken.class);
                // Default to USER role for other tests
                MemberPrincipal principal = new MemberPrincipal(1L, "user@test.com", Role.USER);
                when(authentication.getPrincipal()).thenReturn(principal);
                when(authentication.getDetails()).thenReturn(1L); // userId
                SecurityContextHolder.getContext().setAuthentication(authentication);
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
                LectureSummaryDto dto = LectureSummaryDto.from(lecture, 4.5, 10L);
                when(lectureService.getLectureWithStats(100L)).thenReturn(dto);

                // when & then
                mockMvc.perform(get("/api/v1/lectures/{lectureId}", 100L)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lectureId").value(100))
                                .andExpect(jsonPath("$.lectureName").value("Test Lecture"))
                                .andExpect(jsonPath("$.averageScore").value(4.5));
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
                LectureSummaryDto dto = LectureSummaryDto.from(lecture, 4.2, 5L);
                Page<LectureSummaryDto> page = new PageImpl<>(List.of(dto), Pageable.unpaged(), 1);

                when(lectureService.searchLecturesWithStats(any(LectureSearchCondition.class))).thenReturn(page);

                // when & then
                mockMvc.perform(get("/api/v1/lectures/search")
                                .with(csrf())
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].lectureId").value(100))
                                .andExpect(jsonPath("$.content[0].lectureName").value("Search Result"))
                                .andExpect(jsonPath("$.content[0].steps[0].stepType").value("CODING_TEST"))
                                .andExpect(jsonPath("$.content[0].averageScore").value(4.2));
        }
}
