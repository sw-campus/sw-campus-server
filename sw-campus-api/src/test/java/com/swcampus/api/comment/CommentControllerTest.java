package com.swcampus.api.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swcampus.api.comment.request.CreateCommentRequest;
import com.swcampus.api.comment.request.UpdateCommentRequest;
import com.swcampus.api.comment.response.CommentResponse;
import com.swcampus.api.config.SecurityConfig;
import com.swcampus.domain.auth.TokenProvider;
import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.commentlike.CommentLikeService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, TokenProvider.class})
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private CommentLikeService commentLikeService;

    @MockitoBean
    private CommentResponseMapper commentResponseMapper;

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

        CommentResponse mockResponse = CommentResponse.from(mockComment, "Tester", false, false);

        given(commentService.getCommentsByPostId(anyLong()))
                .willReturn(List.of(mockComment));

        given(commentResponseMapper.toTreeResponse(any(), any()))
                .willReturn(List.of(mockResponse));

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

        given(commentService.updateComment(anyLong(), anyLong(), anyBoolean(), any(), any()))
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
        doNothing().when(commentService).deleteComment(anyLong(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(delete("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 토큰 없음")
    void updateComment_Fail_NoToken() throws Exception {
        // given
        UpdateCommentRequest request = new UpdateCommentRequest(
            "Updated Comment", null
        );

        // when & then
        mockMvc.perform(put("/api/v1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 토큰 없음")
    void deleteComment_Fail_NoToken() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/comments/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없음")
    void updateComment_Fail_AccessDenied() throws Exception {
        // given
        UpdateCommentRequest request = new UpdateCommentRequest(
            "Updated Comment", null
        );

        given(commentService.updateComment(anyLong(), anyLong(), anyBoolean(), any(), any()))
                .willThrow(new com.swcampus.domain.comment.exception.CommentAccessDeniedException("댓글 수정 권한이 없습니다."));

        // when & then
        mockMvc.perform(put("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void deleteComment_Fail_AccessDenied() throws Exception {
        // given
        org.mockito.Mockito.doThrow(new com.swcampus.domain.comment.exception.CommentAccessDeniedException("댓글 삭제 권한이 없습니다."))
                .when(commentService).deleteComment(anyLong(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(delete("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
    void updateComment_Fail_NotFound() throws Exception {
        // given
        UpdateCommentRequest request = new UpdateCommentRequest(
            "Updated Comment", null
        );

        given(commentService.updateComment(anyLong(), anyLong(), anyBoolean(), any(), any()))
                .willThrow(new com.swcampus.domain.comment.exception.CommentNotFoundException(999L));

        // when & then
        mockMvc.perform(put("/api/v1/comments/999")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void deleteComment_Fail_NotFound() throws Exception {
        // given
        org.mockito.Mockito.doThrow(new com.swcampus.domain.comment.exception.CommentNotFoundException(999L))
                .when(commentService).deleteComment(anyLong(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(delete("/api/v1/comments/999")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 본문 없음")
    void createComment_Fail_NoBody() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest(
            1L, null, null, null  // body가 null
        );

        // when & then
        mockMvc.perform(post("/api/v1/comments")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대댓글 작성 성공")
    void createReply_Success() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest(
            1L, 1L, "Reply Comment", null  // parentId 지정
        );

        Comment mockReply = Comment.of(
            2L, 1L, 1L, 1L,  // parentId = 1
            "Reply Comment", null, 0L, false,
            LocalDateTime.now(), LocalDateTime.now()
        );

        given(commentService.createComment(anyLong(), anyLong(), any(), any(), any()))
                .willReturn(mockReply);

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
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.parentId").value(1L))
                .andExpect(jsonPath("$.body").value("Reply Comment"));
    }
}
