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
                .andExpect(jsonPath("$[0].category_id").value(1))
                .andExpect(jsonPath("$[0].category_name").value("Development"))
                .andExpect(jsonPath("$[0].children[0].category_name").value("Backend"));
    }
}
