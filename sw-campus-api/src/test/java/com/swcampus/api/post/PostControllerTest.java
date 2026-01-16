package com.swcampus.api.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.post.request.CreatePostRequest;
import com.swcampus.api.post.response.PostDetailResponse;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.board.BoardCategoryService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({SecurityConfig.class, TokenProvider.class}) // 실제 SecurityConfig와 TokenProvider 사용
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @MockBean
    private PostService postService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private BoardCategoryService boardCategoryService;

    private String validToken;

    @BeforeEach
    void setUp() {
        // 유효한 토큰 생성
        validToken = tokenProvider.createAccessToken(1L, "test@example.com", Role.USER);
    }

    @Test
    @DisplayName("게시글 작성 성공 - 유효한 토큰")
    void createPost_Success() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
            1L, 
            "Test Title", 
            "Test Body", 
            List.of(), 
            List.of("tag1")
        );

        Post mockPost = Post.of(
            1L, 1L, 1L, 
            "Test Title", "Test Body", 
            List.of(), List.of("tag1"), 
            0L, 0L, null, false, 
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        given(postService.createPost(any(), any(), any(), any(), any(), any()))
                .willReturn(mockPost);

        Member mockMember = Member.of(
            1L, "test@example.com", "password", 
            "Test Name", "Tester", "010-1234-5678", 
            Role.USER, null, "Seoul", 
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );

        given(memberService.getMember(anyLong()))
                .willReturn(mockMember);
                
        given(boardCategoryService.getCategoryName(anyLong()))
                .willReturn("Free Board");

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + validToken) // 헤더 인증
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // CSRF는 SecurityConfig에서 disable 했지만 혹시 몰라 추가
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("게시글 작성 실패 - 토큰 없음")
    void createPost_Fail_NoToken() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest(
            1L, "Test Title", "Test Body", List.of(), List.of()
        );

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
