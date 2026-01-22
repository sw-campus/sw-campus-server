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
import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostDetail;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.jsoup.Jsoup;

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
            @Parameter(description = "카테고리 ID") @RequestParam(name = "categoryId", required = false) Long categoryId,
            @Parameter(description = "태그 필터 (복수 가능)") @RequestParam(name = "tags", required = false) List<String> tags,
            @Parameter(description = "검색어 (제목, 본문, 태그 검색)") @RequestParam(name = "keyword", required = false) String keyword,
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

    private static final int PREVIEW_LENGTH = 200;

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 동일 세션에서 1시간 내 재조회 시 조회수가 증가하지 않습니다. 비로그인 사용자에게는 본문 미리보기(200자)만 제공됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPost(
            @OptionalCurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

        Long currentUserId = member != null ? member.memberId() : null;
        boolean isLoggedIn = member != null;

        PostDetail postDetail = postService.getPostDetailWithViewCount(postId, currentUserId);
        Post post = postDetail.getPost();

        boolean isAuthor = currentUserId != null && post.isAuthor(currentUserId);
        boolean isBookmarked = bookmarkService.isBookmarked(currentUserId, postId);
        boolean isLiked = postLikeService.isLiked(currentUserId, postId);

        // 비로그인 사용자에게는 미리보기만 제공
        String bodyContent = isLoggedIn ? post.getBody() : extractPreview(post.getBody());

        PostDetailResponse response = PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(bodyContent)
                .authorId(post.getUserId())
                .authorNickname(postDetail.getAuthorNickname())
                .categoryId(post.getBoardCategoryId())
                .categoryName(postDetail.getCategoryName())
                .images(isLoggedIn ? post.getImages() : List.of())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(postDetail.getCommentCount())
                .selectedCommentId(post.getSelectedCommentId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .bookmarked(isBookmarked)
                .liked(isLiked)
                .pinned(post.isPinned())
                .isAuthor(isAuthor)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * HTML 본문에서 텍스트를 추출하여 미리보기를 생성합니다.
     * 비로그인 사용자에게 제공되는 본문 미리보기입니다.
     * h3 태그(질문/제목)는 제외하고 본문 내용만 추출합니다.
     */
    private String extractPreview(String htmlBody) {
        if (htmlBody == null || htmlBody.isBlank()) {
            return "";
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlBody);
        doc.select("h3").remove();  // 질문/제목 태그 제거
        String textOnly = doc.text();
        if (textOnly.length() <= PREVIEW_LENGTH) {
            return textOnly;
        }
        return textOnly.substring(0, PREVIEW_LENGTH) + "...";
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
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId,
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
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

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
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {
        
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Boolean>> togglePin(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {

        com.swcampus.domain.post.Post post = postService.togglePin(postId);
        return ResponseEntity.ok(java.util.Map.of("pinned", post.isPinned()));
    }

}
