package com.swcampus.api.post;

import com.swcampus.api.post.request.CreatePostRequest;
import com.swcampus.api.post.request.UpdatePostRequest;
import com.swcampus.api.post.response.PostDetailResponse;
import com.swcampus.api.post.response.PostResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.api.security.OptionalCurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.board.BoardCategoryService;
import com.swcampus.domain.bookmark.BookmarkService;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.postlike.PostLikeService;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.Role;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostService;
import com.swcampus.domain.post.PostSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final BoardCategoryService boardCategoryService;
    private final CommentService commentService;
    private final BookmarkService bookmarkService;
    private final PostLikeService postLikeService;


    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody CreatePostRequest request) {

        Post post = postService.createPost(
                member.memberId(),
                request.getBoardCategoryId(),
                request.getTitle(),
                request.getBody(),
                request.getImages(),
                request.getTags()
        );

        String nickname = memberService.getMember(member.memberId()).getNickname();

        String categoryName = boardCategoryService.getCategoryName(request.getBoardCategoryId());


        PostDetailResponse response = PostDetailResponse.from(
                post,
                nickname,
                categoryName,
                0L,        // 댓글 수 (새 게시글이므로 0)
                false,     // 북마크 여부
                false,     // 좋아요 여부
                true       // 본인 작성
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이지네이션으로 조회합니다. 태그 필터링 및 검색을 지원합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @Parameter(description = "카테고리 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "태그 필터 (복수 가능)") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "검색어 (제목, 본문, 태그 검색)") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable) {

        // N+1 문제 해결: JOIN 쿼리로 한 번에 조회
        Page<PostSummary> posts = postService.getPostsWithDetails(categoryId, tags, keyword, pageable);

        if (posts.isEmpty()) {
            return ResponseEntity.ok(Page.empty(pageable));
        }

        // N+1 문제 해결: 댓글 수 일괄 조회
        List<Long> postIds = posts.getContent().stream()
                .map(summary -> summary.getPost().getId())
                .toList();

        Map<Long, Long> commentCounts = commentService.getCommentCounts(postIds);

        Page<PostResponse> response = posts.map(summary ->
            PostResponse.from(
                    summary.getPost(),
                    summary.getAuthorNickname(),
                    summary.getCategoryName(),
                    commentCounts.getOrDefault(summary.getPost().getId(), 0L)
            )
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 조회수가 1 증가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(
            @OptionalCurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {

        Post post = postService.getPostWithViewCount(postId);

        Long currentUserId = member != null ? member.memberId() : null;
        boolean isAuthor = currentUserId != null && post.isAuthor(currentUserId);

        String nickname = "알 수 없음";
        try {
            nickname = memberService.getMember(post.getUserId()).getNickname();
        } catch (MemberNotFoundException e) {
            // 탈퇴한 회원 등 예외 처리
        }

        String categoryName = boardCategoryService.getCategoryName(post.getBoardCategoryId());

        long commentCount = commentService.countByPostId(postId);

        boolean isBookmarked = bookmarkService.isBookmarked(currentUserId, postId);
        boolean isLiked = postLikeService.isLiked(currentUserId, postId);

        PostDetailResponse response = PostDetailResponse.from(
                post,
                nickname,
                categoryName,
                commentCount,
                isBookmarked,
                isLiked,
                isAuthor
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글을 수정합니다. (관리자는 모든 게시글 수정 가능)")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> updatePost(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request) {

        boolean isAdmin = member.role() == Role.ADMIN;

        Post post = postService.updatePost(
                postId,
                member.memberId(),
                isAdmin,
                request.getTitle(),
                request.getBody(),
                request.getImages(),
                request.getTags()
        );

        String nickname = memberService.getMember(member.memberId()).getNickname();

        String categoryName = boardCategoryService.getCategoryName(post.getBoardCategoryId());

        long commentCount = commentService.countByPostId(postId);

        boolean isBookmarked = bookmarkService.isBookmarked(member.memberId(), postId);
        boolean isLiked = postLikeService.isLiked(member.memberId(), postId);

        PostDetailResponse response = PostDetailResponse.from(
                post,
                nickname,
                categoryName,
                commentCount,
                isBookmarked,
                isLiked,
                true       // 본인 작성
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 삭제합니다. (Soft Delete, 관리자는 모든 게시글 삭제 가능)")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {

        boolean isAdmin = member.role() == Role.ADMIN;

        postService.deletePost(postId, member.memberId(), isAdmin);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이전/다음 게시글 조회", description = "현재 게시글의 이전/다음 게시글 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{postId}/adjacent")
    public ResponseEntity<com.swcampus.api.post.response.AdjacentPostResponse> getAdjacentPosts(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {
        
        com.swcampus.domain.post.AdjacentPosts adjacentPosts = postService.getAdjacentPosts(postId);
        com.swcampus.api.post.response.AdjacentPostResponse response = 
                com.swcampus.api.post.response.AdjacentPostResponse.from(adjacentPosts);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 고정/해제", description = "관리자가 게시글을 상단에 고정하거나 해제합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 가능)")
    })
    @PostMapping("/{postId}/pin")
    public ResponseEntity<java.util.Map<String, Boolean>> togglePin(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {
        
        if (member.role() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        
        com.swcampus.domain.post.Post post = postService.togglePin(postId);
        return ResponseEntity.ok(java.util.Map.of("pinned", post.isPinned()));
    }
}
