package com.swcampus.api.bookmark;

import com.swcampus.api.bookmark.response.BookmarkResponse;
import com.swcampus.api.bookmark.response.BookmarkToggleResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.bookmark.Bookmark;
import com.swcampus.domain.bookmark.BookmarkService;
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

@Tag(name = "Bookmark", description = "북마크 API")
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 토글", description = "게시글을 북마크에 추가하거나 해제합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/{postId}")
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {

        boolean bookmarked = bookmarkService.toggleBookmark(member.memberId(), postId);

        BookmarkToggleResponse response = bookmarked
                ? BookmarkToggleResponse.added()
                : BookmarkToggleResponse.removed();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 북마크 목록", description = "로그인한 사용자의 북마크 목록을 조회합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getMyBookmarks(
            @CurrentMember MemberPrincipal member) {

        List<Bookmark> bookmarks = bookmarkService.getMyBookmarks(member.memberId());

        List<BookmarkResponse> response = bookmarks.stream()
                .map(BookmarkResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 여부 확인", description = "특정 게시글의 북마크 여부를 확인합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{postId}/status")
    public ResponseEntity<Boolean> isBookmarked(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {

        boolean bookmarked = bookmarkService.isBookmarked(member.memberId(), postId);

        return ResponseEntity.ok(bookmarked);
    }

    @Operation(summary = "북마크 삭제", description = "게시글을 북마크에서 삭제합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteBookmark(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {

        bookmarkService.deleteBookmark(member.memberId(), postId);

        return ResponseEntity.noContent().build();
    }
}
