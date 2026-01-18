package com.swcampus.api.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.api.post.request.CreatePostRequest;
import com.swcampus.api.post.request.UpdatePostRequest;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private BoardCategoryService boardCategoryService;

    @MockitoBean
    private com.swcampus.domain.comment.CommentService commentService;

    @MockitoBean
    private com.swcampus.domain.bookmark.BookmarkService bookmarkService;

    @MockitoBean
    private com.swcampus.domain.postlike.PostLikeService postLikeService;

    private String validToken;
    private Member mockMember;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        // 유효한 토큰 생성
        validToken = tokenProvider.createAccessToken(1L, "test@example.com", Role.USER);

        mockMember = Member.of(
            1L, "test@example.com", "password",
            "Test Name", "Tester", "010-1234-5678",
            Role.USER, null, "Seoul",
            LocalDateTime.now(), LocalDateTime.now()
        );

        mockPost = Post.of(
            1L, 1L, 1L,
            "Test Title", "Test Body",
            List.of(), List.of("tag1"),
            0L, 0L, 0L, null, false, false,
            LocalDateTime.now(), LocalDateTime.now()
        );
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

        given(postService.createPost(any(), any(), any(), any(), any(), any()))
                .willReturn(mockPost);

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

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated Title", "Updated Body", List.of(), List.of("updated-tag")
        );

        Post updatedPost = Post.of(
            1L, 1L, 1L,
            "Updated Title", "Updated Body",
            List.of(), List.of("updated-tag"),
            0L, 0L, 0L, null, false, false,
            LocalDateTime.now(), LocalDateTime.now()
        );

        given(postService.updatePost(anyLong(), anyLong(), anyBoolean(), any(), any(), any(), any()))
                .willReturn(updatedPost);

        given(memberService.getMember(anyLong()))
                .willReturn(mockMember);

        given(boardCategoryService.getCategoryName(anyLong()))
                .willReturn("Free Board");

        given(commentService.countByPostId(anyLong()))
                .willReturn(0L);

        given(bookmarkService.isBookmarked(any(), anyLong()))
                .willReturn(false);

        given(postLikeService.isLiked(any(), anyLong()))
                .willReturn(false);

        // when & then
        mockMvc.perform(put("/api/v1/posts/1")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() throws Exception {
        // given
        doNothing().when(postService).deletePost(anyLong(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(delete("/api/v1/posts/1")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 토큰 없음")
    void updatePost_Fail_NoToken() throws Exception {
        // given
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated Title", "Updated Body", List.of(), List.of()
        );

        // when & then
        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 토큰 없음")
    void deletePost_Fail_NoToken() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/posts/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

