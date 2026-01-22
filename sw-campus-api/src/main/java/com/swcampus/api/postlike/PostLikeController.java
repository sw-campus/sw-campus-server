package com.swcampus.api.postlike;

import com.swcampus.api.postlike.response.LikeToggleResponse;
import com.swcampus.api.postlike.response.LikerResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.postlike.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post Like", description = "게시글 추천 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 추천 토글", description = "게시글을 추천하거나 추천을 취소합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeToggleResponse> togglePostLike(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

        boolean liked = postLikeService.toggleLike(member.memberId(), postId);

        LikeToggleResponse response = liked
                ? LikeToggleResponse.added()
                : LikeToggleResponse.removed();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 추천 여부 확인", description = "특정 게시글의 추천 여부를 확인합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{postId}/like/status")
    public ResponseEntity<Boolean> isPostLiked(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

        boolean liked = postLikeService.isLiked(member.memberId(), postId);

        return ResponseEntity.ok(liked);
    }

    @Operation(summary = "게시글 추천 목록 조회", description = "게시글을 추천한 사용자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{postId}/likers")
    public ResponseEntity<List<LikerResponse>> getLikers(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

        List<LikerResponse> response = postLikeService.getLikers(postId).stream()
                .map(LikerResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
