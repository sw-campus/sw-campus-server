package com.swcampus.domain.comment;

import com.swcampus.domain.comment.exception.CommentAccessDeniedException;
import com.swcampus.domain.comment.exception.CommentNotFoundException;
import com.swcampus.domain.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Nested
    @DisplayName("댓글 작성")
    class CreateCommentTest {

        @Test
        @DisplayName("댓글 작성 성공")
        void createComment_success() {
            // given
            Long postId = 1L;
            Long userId = 1L;
            String body = "테스트 댓글";

            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> {
                        Comment comment = invocation.getArgument(0);
                        return Comment.of(
                                1L,
                                comment.getPostId(),
                                comment.getUserId(),
                                comment.getParentId(),
                                comment.getBody(),
                                comment.getImageUrl(),
                                0L, false,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        );
                    });

            // when
            Comment result = commentService.createComment(postId, userId, null, body, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getPostId()).isEqualTo(postId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getBody()).isEqualTo(body);
            assertThat(result.getParentId()).isNull();
            verify(postRepository).incrementCommentCount(postId);
        }

        @Test
        @DisplayName("대댓글 작성 성공")
        void createReply_success() {
            // given
            Long postId = 1L;
            Long userId = 1L;
            Long parentId = 10L;
            String body = "대댓글입니다";

            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> {
                        Comment comment = invocation.getArgument(0);
                        return Comment.of(
                                2L,
                                comment.getPostId(),
                                comment.getUserId(),
                                comment.getParentId(),
                                comment.getBody(),
                                comment.getImageUrl(),
                                0L, false,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        );
                    });

            // when
            Comment result = commentService.createComment(postId, userId, parentId, body, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getParentId()).isEqualTo(parentId);
            assertThat(result.isReply()).isTrue();
            verify(postRepository).incrementCommentCount(postId);
        }

        @Test
        @DisplayName("이미지 포함 댓글 작성 성공")
        void createComment_withImage_success() {
            // given
            Long postId = 1L;
            Long userId = 1L;
            String body = "이미지 댓글";
            String imageUrl = "https://example.com/image.jpg";

            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Comment result = commentService.createComment(postId, userId, null, body, imageUrl);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getImageUrl()).isEqualTo(imageUrl);
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class GetCommentTest {

        @Test
        @DisplayName("댓글 조회 성공")
        void getComment_success() {
            // given
            Long commentId = 1L;
            Comment comment = createMockComment(commentId, 1L, 1L, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));

            // when
            Comment result = commentService.getComment(commentId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(commentId);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 조회 시 예외 발생")
        void getComment_notFound_throwsException() {
            // given
            Long commentId = 999L;

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.getComment(commentId))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("게시글별 댓글 목록 조회")
    class GetCommentsByPostIdTest {

        @Test
        @DisplayName("댓글 목록 조회 성공")
        void getCommentsByPostId_success() {
            // given
            Long postId = 1L;
            List<Comment> comments = List.of(
                    createMockComment(1L, postId, 1L, null),
                    createMockComment(2L, postId, 2L, null),
                    createMockComment(3L, postId, 1L, 1L)  // 대댓글
            );

            given(commentRepository.findByPostId(postId))
                    .willReturn(comments);

            // when
            List<Comment> result = commentService.getCommentsByPostId(postId);

            // then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("댓글이 없는 경우 빈 리스트 반환")
        void getCommentsByPostId_empty() {
            // given
            Long postId = 1L;

            given(commentRepository.findByPostId(postId))
                    .willReturn(List.of());

            // when
            List<Comment> result = commentService.getCommentsByPostId(postId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTest {

        @Test
        @DisplayName("본인 댓글 수정 성공")
        void updateComment_success() {
            // given
            Long commentId = 1L;
            Long userId = 1L;
            boolean isAdmin = false;
            String newBody = "수정된 댓글";

            Comment comment = createMockComment(commentId, 1L, userId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Comment result = commentService.updateComment(commentId, userId, isAdmin, newBody, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isEqualTo(newBody);
        }

        @Test
        @DisplayName("관리자가 다른 사용자 댓글 수정 성공")
        void updateComment_admin_success() {
            // given
            Long commentId = 1L;
            Long authorId = 1L;
            Long adminId = 999L;
            boolean isAdmin = true;
            String newBody = "관리자가 수정한 댓글";

            Comment comment = createMockComment(commentId, 1L, authorId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Comment result = commentService.updateComment(commentId, adminId, isAdmin, newBody, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBody()).isEqualTo(newBody);
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시 예외 발생")
        void updateComment_notFound_throwsException() {
            // given
            Long commentId = 999L;

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(commentId, 1L, false, "수정", null))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        @DisplayName("타인 댓글 수정 시 예외 발생")
        void updateComment_accessDenied_throwsException() {
            // given
            Long commentId = 1L;
            Long authorId = 1L;
            Long otherUserId = 2L;
            boolean isAdmin = false;

            Comment comment = createMockComment(commentId, 1L, authorId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(commentId, otherUserId, isAdmin, "수정", null))
                    .isInstanceOf(CommentAccessDeniedException.class);
        }

        @Test
        @DisplayName("이미지 URL 수정 성공")
        void updateComment_withImage_success() {
            // given
            Long commentId = 1L;
            Long userId = 1L;
            String newBody = "수정된 댓글";
            String newImageUrl = "https://example.com/new-image.jpg";

            Comment comment = createMockComment(commentId, 1L, userId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Comment result = commentService.updateComment(commentId, userId, false, newBody, newImageUrl);

            // then
            assertThat(result.getBody()).isEqualTo(newBody);
            assertThat(result.getImageUrl()).isEqualTo(newImageUrl);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTest {

        @Test
        @DisplayName("본인 댓글 삭제 성공")
        void deleteComment_success() {
            // given
            Long commentId = 1L;
            Long postId = 1L;
            Long userId = 1L;
            boolean isAdmin = false;

            Comment comment = createMockComment(commentId, postId, userId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            commentService.deleteComment(commentId, userId, isAdmin);

            // then
            verify(commentRepository).save(any(Comment.class));
            verify(postRepository).decrementCommentCount(postId);
            assertThat(comment.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("관리자가 댓글 삭제 성공")
        void deleteComment_admin_success() {
            // given
            Long commentId = 1L;
            Long postId = 1L;
            Long authorId = 1L;
            Long adminId = 999L;
            boolean isAdmin = true;

            Comment comment = createMockComment(commentId, postId, authorId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            commentService.deleteComment(commentId, adminId, isAdmin);

            // then
            verify(commentRepository).save(any(Comment.class));
            assertThat(comment.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
        void deleteComment_notFound_throwsException() {
            // given
            Long commentId = 999L;

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, 1L, false))
                    .isInstanceOf(CommentNotFoundException.class);

            verify(postRepository, never()).decrementCommentCount(anyLong());
        }

        @Test
        @DisplayName("타인 댓글 삭제 시 예외 발생")
        void deleteComment_accessDenied_throwsException() {
            // given
            Long commentId = 1L;
            Long authorId = 1L;
            Long otherUserId = 2L;
            boolean isAdmin = false;

            Comment comment = createMockComment(commentId, 1L, authorId, null);

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, otherUserId, isAdmin))
                    .isInstanceOf(CommentAccessDeniedException.class);

            verify(postRepository, never()).decrementCommentCount(anyLong());
        }
    }

    @Nested
    @DisplayName("댓글 수 조회")
    class CountCommentTest {

        @Test
        @DisplayName("게시글별 댓글 수 조회 성공")
        void countByPostId_success() {
            // given
            Long postId = 1L;
            long expectedCount = 5L;

            given(commentRepository.countByPostId(postId))
                    .willReturn(expectedCount);

            // when
            long result = commentService.countByPostId(postId);

            // then
            assertThat(result).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("여러 게시글의 댓글 수 일괄 조회 성공")
        void getCommentCounts_success() {
            // given
            List<Long> postIds = List.of(1L, 2L, 3L);
            Map<Long, Long> expectedCounts = Map.of(1L, 5L, 2L, 3L, 3L, 0L);

            given(commentRepository.countByPostIds(postIds))
                    .willReturn(expectedCounts);

            // when
            Map<Long, Long> result = commentService.getCommentCounts(postIds);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(1L)).isEqualTo(5L);
            assertThat(result.get(2L)).isEqualTo(3L);
            assertThat(result.get(3L)).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("사용자별 최근 댓글 조회")
    class GetLatestCommentsByUserAndPostsTest {

        @Test
        @DisplayName("사용자가 특정 게시글들에 단 최근 댓글 조회 성공")
        void getLatestCommentsByUserAndPosts_success() {
            // given
            Long userId = 1L;
            List<Long> postIds = List.of(1L, 2L);
            Comment comment1 = createMockComment(10L, 1L, userId, null);

            given(commentRepository.findLatestByUserIdAndPostIds(userId, postIds))
                    .willReturn(Map.of(1L, comment1));

            // when
            Map<Long, Comment> result = commentService.getLatestCommentsByUserAndPosts(userId, postIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(1L).getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("결과가 없는 경우 빈 Map 반환")
        void getLatestCommentsByUserAndPosts_empty() {
            // given
            Long userId = 1L;
            List<Long> postIds = List.of(1L, 2L);

            given(commentRepository.findLatestByUserIdAndPostIds(userId, postIds))
                    .willReturn(Map.of());

            // when
            Map<Long, Comment> result = commentService.getLatestCommentsByUserAndPosts(userId, postIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("대댓글 수 조회")
    class GetReplyCountsByParentIdsTest {

        @Test
        @DisplayName("여러 댓글의 대댓글 수 조회 성공")
        void getReplyCountsByParentIds_success() {
            // given
            List<Long> commentIds = List.of(1L, 2L);

            given(commentRepository.countRepliesByParentIds(commentIds))
                    .willReturn(Map.of(1L, 3L, 2L, 1L));

            // when
            Map<Long, Long> result = commentService.getReplyCountsByParentIds(commentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(1L)).isEqualTo(3L);
            assertThat(result.get(2L)).isEqualTo(1L);
        }

        @Test
        @DisplayName("빈 목록 전달 시 빈 Map 반환")
        void getReplyCountsByParentIds_empty() {
            // given
            List<Long> commentIds = List.of();

            given(commentRepository.countRepliesByParentIds(commentIds))
                    .willReturn(Map.of());

            // when
            Map<Long, Long> result = commentService.getReplyCountsByParentIds(commentIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글별 댓글 일괄 soft delete")
    class SoftDeleteByPostIdTest {

        @Test
        @DisplayName("게시글의 댓글 일괄 soft delete 성공")
        void softDeleteByPostId_success() {
            // when
            commentService.softDeleteByPostId(1L);

            // then
            verify(commentRepository).softDeleteByPostId(1L);
        }
    }

    @Nested
    @DisplayName("사용자별 댓글 조회")
    class GetCommentsByUserIdTest {

        @Test
        @DisplayName("사용자별 댓글 페이지네이션 조회 성공 및 정렬 순서 확인")
        void getCommentsByUserId_success() {
            // given
            Long userId = 1L;
            Pageable inputPageable = PageRequest.of(0, 10);
            List<Comment> comments = List.of(
                    createMockComment(1L, 1L, userId, null),
                    createMockComment(2L, 2L, userId, null)
            );

            given(commentRepository.findByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(new PageImpl<>(comments, inputPageable, 2));

            // when
            Page<Comment> result = commentService.getCommentsByUserId(userId, inputPageable);

            // then
            assertThat(result.getContent()).hasSize(2);

            // ArgumentCaptor로 전달된 Pageable의 Sort 확인
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(commentRepository).findByUserId(eq(userId), pageableCaptor.capture());

            Sort sort = pageableCaptor.getValue().getSort();
            List<Sort.Order> orders = sort.toList();
            assertThat(orders).hasSize(3);
            assertThat(orders.get(0).getProperty()).isEqualTo("updatedAt");
            assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(orders.get(1).getProperty()).isEqualTo("createdAt");
            assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(orders.get(2).getProperty()).isEqualTo("id");
            assertThat(orders.get(2).getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("결과 없는 경우 빈 페이지 반환")
        void getCommentsByUserId_empty() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            given(commentRepository.findByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(Page.empty(pageable));

            // when
            Page<Comment> result = commentService.getCommentsByUserId(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자별 댓글 수 조회")
    class CountByUserIdTest {

        @Test
        @DisplayName("사용자별 댓글 수 조회 성공")
        void countByUserId_success() {
            // given
            Long userId = 1L;

            given(commentRepository.countByUserId(userId))
                    .willReturn(7L);

            // when
            long result = commentService.countByUserId(userId);

            // then
            assertThat(result).isEqualTo(7L);
        }
    }

    // 헬퍼 메서드
    private Comment createMockComment(Long id, Long postId, Long userId, Long parentId) {
        return Comment.of(
                id,
                postId,
                userId,
                parentId,
                "테스트 댓글",
                null,
                0L,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
