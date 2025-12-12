package com.swcampus.api.cart;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.swcampus.domain.cart.exception.AlreadyInCartException;
import com.swcampus.domain.cart.exception.CartLimitExceededException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.exception.GlobalExceptionHandler;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.cart.CartService;
import com.swcampus.domain.category.Curriculum;
import com.swcampus.domain.lecture.Lecture;
import com.swcampus.domain.lecture.LectureCurriculum;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CartController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("CartController - 스크랩(장바구니) 테스트")
class CartControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private CartService cartService;

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
        @DisplayName("스크랩 추가 성공 (201)")
        void addCart_Add() throws Exception {
                // given
                Long lectureId = 100L;
                // void method, no thenReturn needed

                // when & then
                mockMvc.perform(post("/api/v1/carts")
                                .param("lectureId", String.valueOf(lectureId))
                                .with(csrf()))
                                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("이미 스크랩된 경우 (409)")
        void addCart_Duplicate() throws Exception {
                // given
                Long lectureId = 100L;
                doThrow(new AlreadyInCartException("Already in cart"))
                                .when(cartService).addCart(1L, lectureId);

                // when & then
                mockMvc.perform(post("/api/v1/carts")
                                .param("lectureId", String.valueOf(lectureId))
                                .with(csrf()))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("스크랩 개수 초과 (400)")
        void addCart_LimitExceeded() throws Exception {
                // given
                Long lectureId = 100L;
                doThrow(new CartLimitExceededException("Cart limit exceeded"))
                                .when(cartService).addCart(1L, lectureId);

                // when & then
                mockMvc.perform(post("/api/v1/carts")
                                .param("lectureId", String.valueOf(lectureId))
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("스크랩 삭제 성공 (200)")
        void removeCart_Success() throws Exception {
                // given
                Long lectureId = 100L;

                // when & then
                mockMvc.perform(delete("/api/v1/carts")
                                .param("lectureId", String.valueOf(lectureId))
                                .with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("스크랩 목록 조회 성공")
        void getCart() throws Exception {
                // given
                Long dummyUserId = 1L;
                Long dummyLectureId = 100L;
                Long dummyCategoryId = 123L;

                Curriculum curriculum = Curriculum.builder()
                                .categoryId(dummyCategoryId)
                                .curriculumName("Test Curriculum")
                                .build();

                LectureCurriculum lectureCurriculum = LectureCurriculum.builder()
                                .curriculum(curriculum)
                                .curriculumId(1L)
                                .lectureId(dummyLectureId)
                                .build();

                Lecture lecture = Lecture.builder()
                                .lectureId(dummyLectureId)
                                .lectureName("Test Lecture")
                                .lectureCurriculums(List.of(lectureCurriculum))
                                .build();
                when(cartService.getCartList(dummyUserId)).thenReturn(List.of(lecture));

                // when & then
                mockMvc.perform(get("/api/v1/carts")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].lectureId").value(dummyLectureId))
                                .andExpect(jsonPath("$[0].lectureName").value("Test Lecture"))
                                .andExpect(jsonPath("$[0].categoryId").value(dummyCategoryId));

                verify(cartService).getCartList(dummyUserId);
        }
}
