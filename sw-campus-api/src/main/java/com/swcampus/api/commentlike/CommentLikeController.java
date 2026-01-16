package com.swcampus.api.commentlike;

import com.swcampus.api.postlike.response.LikeToggleResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.commentlike.CommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment Like", description = "댓글 추천 API")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Operation(summary = "댓글 추천 토글", description = "댓글을 추천하거나 추천을 취소합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/{commentId}/like")
    public ResponseEntity<LikeToggleResponse> toggleCommentLike(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId) {

        boolean liked = commentLikeService.toggleLike(member.memberId(), commentId);

        LikeToggleResponse response = liked
                ? LikeToggleResponse.added()
                : LikeToggleResponse.removed();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 추천 여부 확인", description = "특정 댓글의 추천 여부를 확인합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{commentId}/like/status")
    public ResponseEntity<Boolean> isCommentLiked(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId) {

        boolean liked = commentLikeService.isLiked(member.memberId(), commentId);

        return ResponseEntity.ok(liked);
    }
}
