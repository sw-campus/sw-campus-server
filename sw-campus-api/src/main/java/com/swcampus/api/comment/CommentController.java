package com.swcampus.api.comment;

import com.swcampus.api.comment.request.CreateCommentRequest;
import com.swcampus.api.comment.request.UpdateCommentRequest;
import com.swcampus.api.comment.response.CommentResponse;
import com.swcampus.api.notification.SseEmitterService;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.api.security.OptionalCurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.commentlike.CommentLikeService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.notification.Notification;
import com.swcampus.domain.notification.NotificationService;
import com.swcampus.domain.notification.NotificationType;
import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;
    private final CommentLikeService commentLikeService;
    private final CommentResponseMapper commentResponseMapper;
    private final PostService postService;
    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> createComment(
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody CreateCommentRequest request) {

        Comment comment = commentService.createComment(
                request.getPostId(),
                member.memberId(),
                request.getParentId(),
                request.getBody(),
                request.getImageUrl()
        );

        Member commenter = memberService.getMember(member.memberId());
        String nickname = commenter.getNickname();

        // 알림 생성 및 SSE 전송
        sendCommentNotification(comment, commenter, request.getPostId(), request.getParentId());

        CommentResponse response = CommentResponse.from(comment, nickname, true, false);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void sendCommentNotification(Comment comment, Member commenter, Long postId, Long parentId) {
        Long recipientId;
        NotificationType type;

        if (parentId != null) {
            // 대댓글인 경우: 부모 댓글 작성자에게 알림
            Comment parentComment = commentService.getComment(parentId);
            recipientId = parentComment.getUserId();
            type = NotificationType.REPLY;
        } else {
            // 일반 댓글인 경우: 게시글 작성자에게 알림
            Post post = postService.getPost(postId);
            recipientId = post.getUserId();
            type = NotificationType.COMMENT;
        }

        // 알림 생성 (본인에게는 알림을 보내지 않음)
        // targetId에 댓글 ID를 저장 (알림 클릭 시 해당 댓글로 이동)
        Notification notification = notificationService.createNotification(
                recipientId,
                commenter.getId(),
                comment.getId(),  // 댓글 ID
                type
        );

        // SSE로 실시간 전송
        if (notification != null) {
            sseEmitterService.sendNotification(recipientId, notification, commenter.getNickname(), postId);
        }
    }

    @Operation(summary = "게시글별 댓글 목록 조회", description = "게시글의 댓글 목록을 계층 구조로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @OptionalCurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

        Long currentUserId = member != null ? member.memberId() : null;

        List<Comment> comments = commentService.getCommentsByPostId(postId);

        // 계층 구조로 변환
        List<CommentResponse> response = commentResponseMapper.toTreeResponse(comments, currentUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다. (관리자는 모든 댓글 수정 가능)")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentId") Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        boolean isAdmin = member.role() == Role.ADMIN;

        Comment comment = commentService.updateComment(
                commentId,
                member.memberId(),
                isAdmin,
                request.getBody(),
                request.getImageUrl()
        );

        String nickname = memberService.getMember(member.memberId()).getNickname();

        boolean isLiked = commentLikeService.isLiked(member.memberId(), commentId);

        CommentResponse response = CommentResponse.from(comment, nickname, true, isLiked);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다. (Soft Delete, 관리자는 모든 댓글 삭제 가능)")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentId") Long commentId) {

        boolean isAdmin = member.role() == Role.ADMIN;

        commentService.deleteComment(commentId, member.memberId(), isAdmin);

        return ResponseEntity.noContent().build();
    }
}
