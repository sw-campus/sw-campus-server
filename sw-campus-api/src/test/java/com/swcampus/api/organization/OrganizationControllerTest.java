package com.swcampus.api.organization;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureService;
import com.swcampus.domain.lecture.LectureStatus;
import com.swcampus.domain.organization.Organization;
import com.swcampus.domain.organization.OrganizationService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrganizationController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("OrganizationController - 교육기관 테스트")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private LectureService lectureService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("기관 목록 조회 성공")
    void getOrganizationList() throws Exception {
        // given
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        org.springframework.test.util.ReflectionTestUtils.setField(organization, "id", 10L);

        when(organizationService.getOrganizationList(null)).thenReturn(List.of(organization));
        when(lectureService.getRecruitingLectureCounts(anyList())).thenReturn(Map.of(10L, 5L));

        // when & then
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Test Org"))
                .andExpect(jsonPath("$[0].recruiting_lecture_count").value(5));
    }

    @Test
    @DisplayName("기관 상세 조회 성공")
    void getOrganization() throws Exception {
        // given
        Organization organization = Organization.create(1L, "Test Org", "Desc", "url");
        org.springframework.test.util.ReflectionTestUtils.setField(organization, "id", 10L);

        when(organizationService.getOrganization(10L)).thenReturn(organization);

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Org"));
    }

    @Test
    @DisplayName("기관별 강의 목록 조회 성공")
    void getOrganizationLectureList() throws Exception {
        // given
        Lecture lecture = Lecture.builder()
                .lectureId(100L)
                .lectureName("Org Lecture")
                .status(LectureStatus.RECRUITING)
                .build();
        when(lectureService.getLectureListByOrgId(10L)).thenReturn(List.of(lecture));

        // when & then
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/lectures", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lecture_id").value(100))
                .andExpect(jsonPath("$[0].lecture_name").value("Org Lecture"));
    }
}
