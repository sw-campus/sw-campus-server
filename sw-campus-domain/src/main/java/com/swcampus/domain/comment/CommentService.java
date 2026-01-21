package com.swcampus.domain.comment;

import com.swcampus.domain.comment.exception.CommentAccessDeniedException;
import com.swcampus.domain.comment.exception.CommentNotFoundException;
import com.swcampus.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Comment createComment(Long postId, Long userId, Long parentId, String body, String imageUrl) {
        Comment comment = Comment.create(postId, userId, parentId, body, imageUrl);
        Comment saved = commentRepository.save(comment);
        
        // 게시글 댓글 수 증가
        postRepository.incrementCommentCount(postId);
        
        return saved;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    @Transactional
    public Comment updateComment(Long commentId, Long userId, boolean isAdmin, String body, String imageUrl) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!isAdmin && !comment.isAuthor(userId)) {
            throw new CommentAccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.update(body, imageUrl);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!isAdmin && !comment.isAuthor(userId)) {
            throw new CommentAccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        comment.delete();
        commentRepository.save(comment);
        
        // 게시글 댓글 수 감소
        postRepository.decrementCommentCount(comment.getPostId());
    }

    public long countByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    public java.util.Map<Long, Long> getCommentCounts(List<Long> postIds) {
        return commentRepository.countByPostIds(postIds);
    }
}
