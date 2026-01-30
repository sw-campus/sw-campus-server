package com.swcampus.domain.post;

import com.swcampus.domain.board.BoardCategoryService;
import com.swcampus.domain.comment.CommentService;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.post.exception.PostAccessDeniedException;
import com.swcampus.domain.post.exception.PostNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostViewRepository postViewRepository;

    @Mock
    private BoardCategoryService boardCategoryService;

    @Mock
    private MemberService memberService;

    @Mock
    private CommentService commentService;

    @Nested
    @DisplayName("게시글 작성")
    class CreatePostTest {

        @Test
        @DisplayName("게시글 작성 성공")
        void createPost_success() {
            // given
            Long userId = 1L;
            Long boardCategoryId = 1L;
            String title = "테스트 제목";
            String body = "테스트 본문";
            List<String> images = List.of("image1.jpg");
            List<String> tags = List.of("tag1", "tag2");

            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> {
                        Post post = invocation.getArgument(0);
                        return Post.of(
                                1L,
                                post.getBoardCategoryId(),
                                post.getUserId(),
                                post.getTitle(),
                                post.getBody(),
                                post.getImages(),
                                post.getTags(),
                                0L, 0L, 0L, null, false, false,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        );
                    });

            // when
            Post result = postService.createPost(userId, boardCategoryId, title, body, images, tags);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getBoardCategoryId()).isEqualTo(boardCategoryId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getBody()).isEqualTo(body);
            assertThat(result.getImages()).containsExactlyElementsOf(images);
            assertThat(result.getTags()).containsExactlyElementsOf(tags);
        }

        @Test
        @DisplayName("이미지 없이 게시글 작성 성공")
        void createPost_noImages_success() {
            // given
            Long userId = 1L;
            Long boardCategoryId = 1L;
            String title = "테스트 제목";
            String body = "테스트 본문";
            List<String> images = List.of();
            List<String> tags = List.of("tag1");

            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Post result = postService.createPost(userId, boardCategoryId, title, body, images, tags);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getImages()).isEmpty();
        }

        @Test
        @DisplayName("태그 없이 게시글 작성 성공")
        void createPost_noTags_success() {
            // given
            Long userId = 1L;
            Long boardCategoryId = 1L;
            String title = "테스트 제목";
            String body = "테스트 본문";
            List<String> images = List.of();
            List<String> tags = List.of();

            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Post result = postService.createPost(userId, boardCategoryId, title, body, images, tags);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글 조회")
    class GetPostTest {

        @Test
        @DisplayName("게시글 조회 성공")
        void getPost_success() {
            // given
            Long postId = 1L;
            Post post = createMockPost(postId, 1L, 1L);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));

            // when
            Post result = postService.getPost(postId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(postId);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
        void getPost_notFound_throwsException() {
            // given
            Long postId = 999L;

            given(postRepository.findById(postId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPost(postId))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("게시글 조회 (조회수 증가)")
    class GetPostWithViewCountTest {

        @Test
        @DisplayName("게시글 조회 시 조회수 증가")
        void getPostWithViewCount_success() {
            // given
            Long postId = 1L;
            Post post = createMockPost(postId, 1L, 1L);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));

            // when
            Post result = postService.getPostWithViewCount(postId);

            // then
            assertThat(result).isNotNull();
            verify(postRepository).incrementViewCount(postId);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
        void getPostWithViewCount_notFound_throwsException() {
            // given
            Long postId = 999L;

            given(postRepository.findById(postId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPostWithViewCount(postId))
                    .isInstanceOf(PostNotFoundException.class);

            verify(postRepository, never()).incrementViewCount(anyLong());
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회")
    class GetPostsTest {

        @Test
        @DisplayName("전체 게시글 목록 조회 성공")
        void getPosts_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(
                    createMockPost(1L, 1L, 1L),
                    createMockPost(2L, 1L, 2L)
            );
            Page<Post> postPage = new PageImpl<>(posts, pageable, 2);

            given(postRepository.findAll(any(), any(), any()))
                    .willReturn(postPage);

            // when
            Page<Post> result = postService.getPosts(null, null, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("카테고리 필터로 게시글 목록 조회")
        void getPosts_withCategory_success() {
            // given
            Long categoryId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<Long> childCategoryIds = List.of(1L, 2L);

            given(boardCategoryService.getChildCategoryIds(categoryId))
                    .willReturn(childCategoryIds);
            given(postRepository.findAll(any(), any(), any()))
                    .willReturn(Page.empty(pageable));

            // when
            Page<Post> result = postService.getPosts(categoryId, null, pageable);

            // then
            assertThat(result).isNotNull();
            verify(boardCategoryService).getChildCategoryIds(categoryId);
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회 (상세 정보 포함)")
    class GetPostsWithDetailsTest {

        @Test
        @DisplayName("게시글 목록 상세 조회 성공")
        void getPostsWithDetails_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            given(postRepository.findAllWithDetails(any(), any(), any(), any()))
                    .willReturn(Page.empty(pageable));

            // when
            Page<PostSummary> result = postService.getPostsWithDetails(null, null, null, pageable);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("검색어로 게시글 목록 조회")
        void getPostsWithDetails_withKeyword_success() {
            // given
            String keyword = "테스트";
            Pageable pageable = PageRequest.of(0, 10);

            given(postRepository.findAllWithDetails(any(), any(), any(), any()))
                    .willReturn(Page.empty(pageable));

            // when
            Page<PostSummary> result = postService.getPostsWithDetails(null, null, keyword, pageable);

            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePostTest {

        @Test
        @DisplayName("본인 게시글 수정 성공")
        void updatePost_success() {
            // given
            Long postId = 1L;
            Long userId = 1L;
            boolean isAdmin = false;
            String newTitle = "수정된 제목";
            String newBody = "수정된 본문";
            List<String> newImages = List.of("new_image.jpg");
            List<String> newTags = List.of("new_tag");

            Post post = createMockPost(postId, 1L, userId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Post result = postService.updatePost(postId, userId, isAdmin, newTitle, newBody, newImages, newTags);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(newTitle);
            assertThat(result.getBody()).isEqualTo(newBody);
        }

        @Test
        @DisplayName("관리자가 다른 사용자 게시글 수정 성공")
        void updatePost_admin_success() {
            // given
            Long postId = 1L;
            Long authorId = 1L;
            Long adminId = 999L;
            boolean isAdmin = true;
            String newTitle = "관리자가 수정한 제목";
            String newBody = "관리자가 수정한 본문";

            Post post = createMockPost(postId, 1L, authorId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Post result = postService.updatePost(postId, adminId, isAdmin, newTitle, newBody, List.of(), List.of());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
        void updatePost_notFound_throwsException() {
            // given
            Long postId = 999L;

            given(postRepository.findById(postId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.updatePost(
                    postId, 1L, false, "제목", "본문", List.of(), List.of()
            )).isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("타인 게시글 수정 시 예외 발생")
        void updatePost_accessDenied_throwsException() {
            // given
            Long postId = 1L;
            Long authorId = 1L;
            Long otherUserId = 2L;
            boolean isAdmin = false;

            Post post = createMockPost(postId, 1L, authorId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(
                    postId, otherUserId, isAdmin, "제목", "본문", List.of(), List.of()
            )).isInstanceOf(PostAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePostTest {

        @Test
        @DisplayName("본인 게시글 삭제 성공")
        void deletePost_success() {
            // given
            Long postId = 1L;
            Long userId = 1L;
            boolean isAdmin = false;

            Post post = createMockPost(postId, 1L, userId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            postService.deletePost(postId, userId, isAdmin);

            // then
            verify(postRepository).save(any(Post.class));
            verify(commentService).softDeleteByPostId(postId);
            assertThat(post.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("관리자가 게시글 삭제 성공")
        void deletePost_admin_success() {
            // given
            Long postId = 1L;
            Long authorId = 1L;
            Long adminId = 999L;
            boolean isAdmin = true;

            Post post = createMockPost(postId, 1L, authorId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            postService.deletePost(postId, adminId, isAdmin);

            // then
            verify(postRepository).save(any(Post.class));
            verify(commentService).softDeleteByPostId(postId);
            assertThat(post.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
        void deletePost_notFound_throwsException() {
            // given
            Long postId = 999L;

            given(postRepository.findById(postId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.deletePost(postId, 1L, false))
                    .isInstanceOf(PostNotFoundException.class);

            verify(commentService, never()).softDeleteByPostId(anyLong());
        }

        @Test
        @DisplayName("타인 게시글 삭제 시 예외 발생")
        void deletePost_accessDenied_throwsException() {
            // given
            Long postId = 1L;
            Long authorId = 1L;
            Long otherUserId = 2L;
            boolean isAdmin = false;

            Post post = createMockPost(postId, 1L, authorId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.deletePost(postId, otherUserId, isAdmin))
                    .isInstanceOf(PostAccessDeniedException.class);

            verify(commentService, never()).softDeleteByPostId(anyLong());
        }
    }

    @Nested
    @DisplayName("답변 채택")
    class SelectCommentTest {

        @Test
        @DisplayName("답변 채택 성공")
        void selectComment_success() {
            // given
            Long postId = 1L;
            Long userId = 1L;
            Long commentId = 10L;

            Post post = createMockPost(postId, 1L, userId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            postService.selectComment(postId, userId, commentId);

            // then
            verify(postRepository).save(any(Post.class));
            assertThat(post.getSelectedCommentId()).isEqualTo(commentId);
        }

        @Test
        @DisplayName("타인 게시글의 답변 채택 시 예외 발생")
        void selectComment_accessDenied_throwsException() {
            // given
            Long postId = 1L;
            Long authorId = 1L;
            Long otherUserId = 2L;
            Long commentId = 10L;

            Post post = createMockPost(postId, 1L, authorId);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.selectComment(postId, otherUserId, commentId))
                    .isInstanceOf(PostAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("이전/다음 게시글 조회")
    class GetAdjacentPostsTest {

        @Test
        @DisplayName("이전/다음 게시글 모두 있는 경우")
        void getAdjacentPosts_bothExist() {
            // given
            Long postId = 5L;
            PostSummary prevPost = createMockPostSummary(4L);
            PostSummary nextPost = createMockPostSummary(6L);

            given(postRepository.findPreviousPost(postId))
                    .willReturn(Optional.of(prevPost));
            given(postRepository.findNextPost(postId))
                    .willReturn(Optional.of(nextPost));

            // when
            AdjacentPosts result = postService.getAdjacentPosts(postId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPrevious()).isNotNull();
            assertThat(result.getPrevious().getPost().getId()).isEqualTo(4L);
            assertThat(result.getNext()).isNotNull();
            assertThat(result.getNext().getPost().getId()).isEqualTo(6L);
        }

        @Test
        @DisplayName("이전 게시글만 있는 경우 (마지막 게시글)")
        void getAdjacentPosts_onlyPrev() {
            // given
            Long postId = 10L;
            PostSummary prevPost = createMockPostSummary(9L);

            given(postRepository.findPreviousPost(postId))
                    .willReturn(Optional.of(prevPost));
            given(postRepository.findNextPost(postId))
                    .willReturn(Optional.empty());

            // when
            AdjacentPosts result = postService.getAdjacentPosts(postId);

            // then
            assertThat(result.getPrevious()).isNotNull();
            assertThat(result.getNext()).isNull();
        }

        @Test
        @DisplayName("다음 게시글만 있는 경우 (첫 번째 게시글)")
        void getAdjacentPosts_onlyNext() {
            // given
            Long postId = 1L;
            PostSummary nextPost = createMockPostSummary(2L);

            given(postRepository.findPreviousPost(postId))
                    .willReturn(Optional.empty());
            given(postRepository.findNextPost(postId))
                    .willReturn(Optional.of(nextPost));

            // when
            AdjacentPosts result = postService.getAdjacentPosts(postId);

            // then
            assertThat(result.getPrevious()).isNull();
            assertThat(result.getNext()).isNotNull();
        }

        @Test
        @DisplayName("이전/다음 게시글 모두 없는 경우 (유일한 게시글)")
        void getAdjacentPosts_noneExist() {
            // given
            Long postId = 1L;

            given(postRepository.findPreviousPost(postId))
                    .willReturn(Optional.empty());
            given(postRepository.findNextPost(postId))
                    .willReturn(Optional.empty());

            // when
            AdjacentPosts result = postService.getAdjacentPosts(postId);

            // then
            assertThat(result.getPrevious()).isNull();
            assertThat(result.getNext()).isNull();
        }
    }

    @Nested
    @DisplayName("게시글 고정/해제")
    class TogglePinTest {

        @Test
        @DisplayName("고정되지 않은 게시글 고정")
        void togglePin_pin_success() {
            // given
            Long postId = 1L;
            Post post = createMockPost(postId, 1L, 1L);

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Post result = postService.togglePin(postId);

            // then
            assertThat(result.isPinned()).isTrue();
        }

        @Test
        @DisplayName("고정된 게시글 해제")
        void togglePin_unpin_success() {
            // given
            Long postId = 1L;
            Post post = Post.of(
                    postId, 1L, 1L,
                    "제목", "본문",
                    List.of(), List.of(),
                    0L, 0L, 0L, null, true, false,  // pinned = true
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(postRepository.findById(postId))
                    .willReturn(Optional.of(post));
            given(postRepository.save(any(Post.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Post result = postService.togglePin(postId);

            // then
            assertThat(result.isPinned()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 게시글 고정 시 예외 발생")
        void togglePin_notFound_throwsException() {
            // given
            Long postId = 999L;

            given(postRepository.findById(postId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.togglePin(postId))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("댓글 단 게시글 조회")
    class GetCommentedPostsByUserIdTest {

        @Test
        @DisplayName("사용자가 댓글 단 게시글 목록 조회 성공")
        void getCommentedPostsByUserId_success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            List<PostSummary> summaries = List.of(
                    createMockPostSummary(1L),
                    createMockPostSummary(2L)
            );

            given(postRepository.findCommentedByUserId(userId, pageable))
                    .willReturn(new PageImpl<>(summaries, pageable, 2));

            // when
            Page<PostSummary> result = postService.getCommentedPostsByUserId(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("결과 없는 경우 빈 페이지 반환")
        void getCommentedPostsByUserId_empty() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            given(postRepository.findCommentedByUserId(userId, pageable))
                    .willReturn(Page.empty(pageable));

            // when
            Page<PostSummary> result = postService.getCommentedPostsByUserId(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자별 게시글 수 조회")
    class CountByUserIdTest {

        @Test
        @DisplayName("사용자별 게시글 수 조회 성공")
        void countByUserId_success() {
            // given
            Long userId = 1L;

            given(postRepository.countByUserId(userId))
                    .willReturn(5L);

            // when
            long result = postService.countByUserId(userId);

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    // 헬퍼 메서드
    private Post createMockPost(Long id, Long boardCategoryId, Long userId) {
        return Post.of(
                id,
                boardCategoryId,
                userId,
                "테스트 제목",
                "테스트 본문",
                List.of(),
                List.of("tag1"),
                0L, 0L, 0L, null, false, false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private PostSummary createMockPostSummary(Long postId) {
        Post post = createMockPost(postId, 1L, 1L);
        return PostSummary.builder()
                .post(post)
                .authorNickname("테스터")
                .categoryName("자유게시판")
                .build();
    }
}
