package com.swcampus.api.category;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.category.Category;
import com.swcampus.domain.category.CategoryService;
import com.swcampus.domain.category.Curriculum;

@WebMvcTest(controllers = CategoryController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("CategoryController - 카테고리 테스트")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("카테고리 트리 조회 성공")
    void getCategoryTree() throws Exception {
        // given
        Category subCategory = Category.builder().categoryId(2L).categoryName("Backend").build();
        Category rootCategory = Category.builder()
                .categoryId(1L)
                .categoryName("Development")
                .children(List.of(subCategory))
                .build();

        when(categoryService.getCategoryTree()).thenReturn(List.of(rootCategory));

        // when & then
        mockMvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Development"))
                .andExpect(jsonPath("$[0].children[0].categoryName").value("Backend"));
    }

    @Test
    @DisplayName("카테고리별 커리큘럼 조회 성공")
    void getCurriculumsByCategoryId() throws Exception {
        // given
        Long categoryId = 111L;
        List<Curriculum> curriculums = List.of(
                Curriculum.builder()
                        .curriculumId(1L)
                        .categoryId(categoryId)
                        .curriculumName("React 기초")
                        .build(),
                Curriculum.builder()
                        .curriculumId(2L)
                        .categoryId(categoryId)
                        .curriculumName("React Hooks 마스터")
                        .build());

        when(categoryService.getCurriculumsByCategoryId(categoryId)).thenReturn(curriculums);

        // when & then
        mockMvc.perform(get("/api/v1/categories/{categoryId}/curriculums", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].curriculumId").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(categoryId))
                .andExpect(jsonPath("$[0].curriculumName").value("React 기초"))
                .andExpect(jsonPath("$[1].curriculumId").value(2))
                .andExpect(jsonPath("$[1].curriculumName").value("React Hooks 마스터"));
    }

    @Test
    @DisplayName("카테고리별 커리큘럼 조회 - 빈 결과")
    void getCurriculumsByCategoryId_empty() throws Exception {
        // given
        Long categoryId = 999L;
        when(categoryService.getCurriculumsByCategoryId(categoryId)).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/categories/{categoryId}/curriculums", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
