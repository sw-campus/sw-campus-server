package com.swcampus.domain.post;

import com.swcampus.domain.board.BoardCategoryService;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.member.exception.MemberNotFoundException;
import com.swcampus.domain.post.exception.PostAccessDeniedException;
import com.swcampus.domain.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final String UNKNOWN_AUTHOR = "알 수 없음";

    private final PostRepository postRepository;
    private final BoardCategoryService boardCategoryService;
    private final MemberService memberService;
    private final CommentService commentService;

    @Transactional
    public Post createPost(Long userId, Long boardCategoryId, String title, String body,
                           List<String> images, List<String> tags) {
        Post post = Post.create(boardCategoryId, userId, title, body, images, tags);
        return postRepository.save(post);
    }

    public Page<Post> getPosts(Long categoryId, List<String> tags, Pageable pageable) {
        List<Long> categoryIds = (categoryId != null)
                ? boardCategoryService.getChildCategoryIds(categoryId)
                : null;
        return postRepository.findAll(categoryIds, tags, pageable);
    }

    /**
     * 게시글 목록을 작성자 닉네임, 카테고리 이름과 함께 조회합니다.
     * N+1 문제를 해결하기 위해 JOIN 쿼리를 사용합니다.
     * @param keyword 검색어 (제목, 본문, 태그에서 검색)
     */
    public Page<PostSummary> getPostsWithDetails(Long categoryId, List<String> tags, String keyword, Pageable pageable) {
        List<Long> categoryIds = (categoryId != null)
                ? boardCategoryService.getChildCategoryIds(categoryId)
                : null;
        return postRepository.findAllWithDetails(categoryIds, tags, keyword, pageable);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    @Transactional
    public Post getPostWithViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        postRepository.incrementViewCount(postId);
        post.incrementViewCount();

        return post;
    }

    /**
     * 게시글 상세 정보를 조회합니다. 조회수가 1 증가합니다.
     * 탈퇴한 회원의 경우 작성자 닉네임이 "알 수 없음"으로 표시됩니다.
     */
    @Transactional
    public PostDetail getPostDetailWithViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        postRepository.incrementViewCount(postId);
        post.incrementViewCount();

        String authorNickname = getAuthorNickname(post.getUserId());
        String categoryName = boardCategoryService.getCategoryName(post.getBoardCategoryId());
        long commentCount = commentService.countByPostId(postId);

        return PostDetail.builder()
                .post(post)
                .authorNickname(authorNickname)
                .categoryName(categoryName)
                .commentCount(commentCount)
                .build();
    }

    /**
     * 작성자 닉네임을 조회합니다.
     * 탈퇴한 회원의 경우 "알 수 없음"을 반환합니다.
     */
    private String getAuthorNickname(Long userId) {
        try {
            return memberService.getMember(userId).getNickname();
        } catch (MemberNotFoundException e) {
            return UNKNOWN_AUTHOR;
        }
    }

    @Transactional
    public Post updatePost(Long postId, Long userId, boolean isAdmin, String title, String body,
                           List<String> images, List<String> tags) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!isAdmin && !post.isAuthor(userId)) {
            throw new PostAccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        post.update(title, body, images, tags);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!isAdmin && !post.isAuthor(userId)) {
            throw new PostAccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        post.delete();
        postRepository.save(post);
    }

    @Transactional
    public void selectComment(Long postId, Long userId, Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.isAuthor(userId)) {
            throw new PostAccessDeniedException("답변 채택 권한이 없습니다.");
        }

        post.selectComment(commentId);
        postRepository.save(post);
    }

    /**
     * 이전/다음 게시글 조회
     */
    public AdjacentPosts getAdjacentPosts(Long postId) {
        PostSummary prev = postRepository.findPreviousPost(postId).orElse(null);
        PostSummary next = postRepository.findNextPost(postId).orElse(null);
        return new AdjacentPosts(prev, next);
    }

    /**
     * 게시글 고정/해제 토글 (관리자만 가능)
     */
    @Transactional
    public Post togglePin(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        post.togglePin();
        return postRepository.save(post);
    }
}
