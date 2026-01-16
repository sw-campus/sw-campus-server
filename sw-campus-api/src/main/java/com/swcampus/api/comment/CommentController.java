package com.swcampus.api.comment;

import com.swcampus.api.comment.request.CreateCommentRequest;
import com.swcampus.api.comment.request.UpdateCommentRequest;
import com.swcampus.api.comment.response.CommentResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.api.security.OptionalCurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.commentlike.CommentLikeService;
import com.swcampus.domain.member.MemberService;

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

import com.swcampus.domain.member.Member;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;
    private final CommentLikeService commentLikeService;

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

        String nickname = memberService.getMember(member.memberId()).getNickname();

        CommentResponse response = CommentResponse.from(comment, nickname, true, false);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "게시글별 댓글 목록 조회", description = "게시글의 댓글 목록을 계층 구조로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @OptionalCurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {

        Long currentUserId = member != null ? member.memberId() : null;

        List<Comment> comments = commentService.getCommentsByPostId(postId);

        // 계층 구조로 변환
        List<CommentResponse> response = buildCommentTree(comments, currentUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
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
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        Comment comment = commentService.updateComment(
                commentId,
                member.memberId(),
                request.getBody(),
                request.getImageUrl()
        );

        String nickname = memberService.getMember(member.memberId()).getNickname();

        boolean isLiked = commentLikeService.isLiked(member.memberId(), commentId);

        CommentResponse response = CommentResponse.from(comment, nickname, true, isLiked);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다. (Soft Delete)")
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
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId) {

        commentService.deleteComment(commentId, member.memberId());

        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 목록을 계층 구조(부모-자식)로 변환합니다.
     * N+1 문제를 해결하기 위해 작성자 정보를 일괄 조회합니다.
     */
    private List<CommentResponse> buildCommentTree(List<Comment> comments, Long currentUserId) {
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 작성자 ID 목록 수집
        List<Long> authorIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .toList();

        // 2. 작성자 정보 일괄 조회
        Map<Long, String> nicknameMap = memberService.getMembersByIds(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        Map<Long, CommentResponse> commentMap = new LinkedHashMap<>();
        List<CommentResponse> rootComments = new ArrayList<>();

        // 3. 사용자가 추천한 댓글 ID 목록 조회
        Set<Long> likedCommentIds = commentLikeService.getLikedCommentIds(currentUserId);

        // 4. 모든 댓글을 CommentResponse로 변환하고 Map에 저장
        for (Comment comment : comments) {
            String nickname = nicknameMap.getOrDefault(comment.getUserId(), "알 수 없음");
            boolean isAuthor = currentUserId != null && comment.isAuthor(currentUserId);
            boolean isLiked = likedCommentIds.contains(comment.getId());
            CommentResponse response = CommentResponse.from(comment, nickname, isAuthor, isLiked);
            commentMap.put(comment.getId(), response);
        }

        // 4. 부모-자식 관계 설정
        for (Comment comment : comments) {
            CommentResponse response = commentMap.get(comment.getId());
            if (comment.getParentId() == null) {
                // 루트 댓글
                rootComments.add(response);
            } else {
                // 대댓글: 부모 댓글에 추가
                CommentResponse parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.addReply(response);
                } else {
                    // 부모가 삭제된 경우 루트로 표시
                    rootComments.add(response);
                }
            }
        }

        return rootComments;
    }
}
