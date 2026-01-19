package com.swcampus.infra.postgres.commentlike;

import com.swcampus.domain.commentlike.CommentLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_likes_seq")
    @SequenceGenerator(name = "comment_likes_seq", sequenceName = "comment_likes_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static CommentLikeEntity from(CommentLike commentLike) {
        CommentLikeEntity entity = new CommentLikeEntity();
        entity.id = commentLike.getId();
        entity.userId = commentLike.getUserId();
        entity.commentId = commentLike.getCommentId();
        entity.createdAt = commentLike.getCreatedAt();
        return entity;
    }

    public CommentLike toDomain() {
        return CommentLike.of(
                this.id,
                this.userId,
                this.commentId,
                this.createdAt
        );
    }
}
