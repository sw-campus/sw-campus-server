package com.swcampus.api.user;

import com.swcampus.api.post.response.PostResponse;
import com.swcampus.api.user.response.UserProfileResponse;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.post.PostRepository;
import com.swcampus.domain.post.PostSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User Profile", description = "유저 공개 프로필 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final MemberService memberService;
    private final PostRepository postRepository;
    private final CommentService commentService;

    @Operation(summary = "유저 프로필 조회", description = "특정 유저의 공개 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(description = "유저 ID", required = true) @PathVariable Long userId) {
        
        Member member = memberService.getMember(userId);
        long postCount = postRepository.countByUserId(userId);

        UserProfileResponse response = UserProfileResponse.of(
                member.getId(),
                member.getNickname(),
                member.getCreatedAt(),
                postCount
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "유저 게시글 목록 조회", description = "특정 유저가 작성한 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            @Parameter(description = "유저 ID", required = true) @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable) {

        // 유저 존재 여부 확인
        Member member = memberService.getMember(userId);
        String authorNickname = member.getNickname();

        Page<PostSummary> posts = postRepository.findByUserId(userId, pageable);

        if (posts.isEmpty()) {
            return ResponseEntity.ok(Page.empty(pageable));
        }

        // 댓글 수 일괄 조회
        List<Long> postIds = posts.getContent().stream()
                .map(summary -> summary.getPost().getId())
                .toList();

        Map<Long, Long> commentCounts = commentService.getCommentCounts(postIds);

        Page<PostResponse> response = posts.map(summary ->
                PostResponse.from(
                        summary.getPost(),
                        authorNickname,
                        summary.getCategoryName(),
                        commentCounts.getOrDefault(summary.getPost().getId(), 0L)
                )
        );

        return ResponseEntity.ok(response);
    }
}
