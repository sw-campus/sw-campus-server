package com.swcampus.domain.bookmark;

import com.swcampus.domain.post.PostRepository;
import com.swcampus.domain.post.PostSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;

    /**
     * 북마크 토글 - 이미 북마크되어 있으면 삭제, 없으면 추가
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return true: 북마크 추가됨, false: 북마크 삭제됨
     */
    @Transactional
    public boolean toggleBookmark(Long userId, Long postId) {
        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
            return false;
        } else {
            Bookmark bookmark = Bookmark.create(userId, postId);
            bookmarkRepository.save(bookmark);
            return true;
        }
    }

    /**
     * 특정 게시글 북마크 여부 확인
     */
    public boolean isBookmarked(Long userId, Long postId) {
        if (userId == null) {
            return false;
        }
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }

    /**
     * 사용자의 북마크 목록 조회
     */
    public List<Bookmark> getMyBookmarks(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    /**
     * 사용자가 북마크한 게시글 ID 목록 조회 (일괄 조회용)
     */
    public Set<Long> getBookmarkedPostIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return bookmarkRepository.findPostIdsByUserId(userId);
    }

    /**
     * 북마크 삭제
     */
    @Transactional
    public void deleteBookmark(Long userId, Long postId) {
        bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
    }

    /**
     * 사용자의 북마크 목록을 게시글 정보와 함께 조회
     */
    public List<BookmarkWithPost> getMyBookmarksWithPosts(Long userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);

        if (bookmarks.isEmpty()) {
            return List.of();
        }

        // 북마크된 게시글 ID 목록
        List<Long> postIds = bookmarks.stream()
                .map(Bookmark::getPostId)
                .toList();

        // 게시글 정보 조회 (삭제된 게시글은 제외됨)
        List<PostSummary> posts = postRepository.findAllByIds(postIds);

        // postId -> PostSummary 맵
        Map<Long, PostSummary> postMap = posts.stream()
                .collect(Collectors.toMap(
                        ps -> ps.getPost().getId(),
                        Function.identity()
                ));

        // 북마크와 게시글 정보 조합 (삭제된 게시글은 제외)
        return bookmarks.stream()
                .filter(bookmark -> postMap.containsKey(bookmark.getPostId()))
                .map(bookmark -> BookmarkWithPost.of(bookmark, postMap.get(bookmark.getPostId())))
                .toList();
    }
}
