package com.swcampus.infra.postgres.comment;

import com.swcampus.domain.comment.Comment;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_seq")
    @SequenceGenerator(name = "comments_seq", sequenceName = "comments_comment_id_seq", allocationSize = 1)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_pid")
    private Long parentId;

    @Column(name = "comment_body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "comment_image_url")
    private String imageUrl;

    @Column(name = "comment_like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public static CommentEntity from(Comment comment) {
        CommentEntity entity = new CommentEntity();
        entity.id = comment.getId();
        entity.postId = comment.getPostId();
        entity.userId = comment.getUserId();
        entity.parentId = comment.getParentId();
        entity.body = comment.getBody();
        entity.imageUrl = comment.getImageUrl();
        entity.likeCount = comment.getLikeCount();
        entity.deleted = comment.isDeleted();
        return entity;
    }

    public void update(Comment comment) {
        this.body = comment.getBody();
        this.imageUrl = comment.getImageUrl();
        this.deleted = comment.isDeleted();
    }

    public Comment toDomain() {
        return Comment.of(
                this.id,
                this.postId,
                this.userId,
                this.parentId,
                this.body,
                this.imageUrl,
                this.likeCount,
                this.deleted,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
