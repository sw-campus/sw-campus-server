package com.swcampus.api.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.comment.request.CreateCommentRequest;
import com.swcampus.api.comment.request.UpdateCommentRequest;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, TokenProvider.class})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @MockBean
    private CommentService commentService;

    @MockBean
    private MemberService memberService;

    private String validToken;
    private Member mockMember;

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
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_Success() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest(
            1L, null, "Test Comment", null
        );

        Comment mockComment = Comment.of(
            1L, 1L, 1L, null,
            "Test Comment", null, 0L, false,
            LocalDateTime.now(), LocalDateTime.now()
        );

        given(commentService.createComment(anyLong(), anyLong(), any(), any(), any()))
                .willReturn(mockComment);

        given(memberService.getMember(anyLong()))
                .willReturn(mockMember);

        // when & then
        mockMvc.perform(post("/api/v1/comments")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.body").value("Test Comment"))
                .andExpect(jsonPath("$.authorNickname").value("Tester"));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 토큰 없음")
    void createComment_Fail_NoToken() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest(
            1L, null, "Test Comment", null
        );

        // when & then
        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글별 댓글 목록 조회 성공")
    void getCommentsByPostId_Success() throws Exception {
        // given
        Comment mockComment = Comment.of(
            1L, 1L, 1L, null,
            "Test Comment", null, 0L, false,
            LocalDateTime.now(), LocalDateTime.now()
        );

        given(commentService.getCommentsByPostId(anyLong()))
                .willReturn(List.of(mockComment));

        given(memberService.getMember(anyLong()))
                .willReturn(mockMember);

        // when & then
        mockMvc.perform(get("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].body").value("Test Comment"));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() throws Exception {
        // given
        UpdateCommentRequest request = new UpdateCommentRequest(
            "Updated Comment", null
        );

        Comment mockComment = Comment.of(
            1L, 1L, 1L, null,
            "Updated Comment", null, 0L, false,
            LocalDateTime.now(), LocalDateTime.now()
        );

        given(commentService.updateComment(anyLong(), anyLong(), any(), any()))
                .willReturn(mockComment);

        given(memberService.getMember(anyLong()))
                .willReturn(mockMember);

        // when & then
        mockMvc.perform(put("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated Comment"));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() throws Exception {
        // given
        doNothing().when(commentService).deleteComment(anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
