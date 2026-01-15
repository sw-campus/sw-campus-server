package com.swcampus.infra.postgres.post;

import com.swcampus.domain.post.Post;
import com.swcampus.infra.postgres.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq")
    @SequenceGenerator(name = "posts_seq", sequenceName = "posts_post_id_seq", allocationSize = 1)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "board_category_id", nullable = false)
    private Long boardCategoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_title", nullable = false)
    private String title;

    @Column(name = "post_body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "post_images", columnDefinition = "TEXT[]")
    private List<String> images = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "TEXT[]")
    private List<String> tags = new ArrayList<>();

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "selected_comment_id")
    private Long selectedCommentId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public static PostEntity from(Post post) {
        PostEntity entity = new PostEntity();
        entity.id = post.getId();
        entity.boardCategoryId = post.getBoardCategoryId();
        entity.userId = post.getUserId();
        entity.title = post.getTitle();
        entity.body = post.getBody();
        entity.images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        entity.tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();
        entity.viewCount = post.getViewCount();
        entity.likeCount = post.getLikeCount();
        entity.selectedCommentId = post.getSelectedCommentId();
        entity.deleted = post.isDeleted();
        return entity;
    }

    public void update(Post post) {
        this.title = post.getTitle();
        this.body = post.getBody();
        this.images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        this.tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();
        this.selectedCommentId = post.getSelectedCommentId();
        this.deleted = post.isDeleted();
    }

    public Post toDomain() {
        return Post.of(
                this.id,
                this.boardCategoryId,
                this.userId,
                this.title,
                this.body,
                this.images != null ? new ArrayList<>(this.images) : new ArrayList<>(),
                this.tags != null ? new ArrayList<>(this.tags) : new ArrayList<>(),
                this.viewCount,
                this.likeCount,
                this.selectedCommentId,
                this.deleted,
                this.getCreatedAt(),
                this.getUpdatedAt()
        );
    }
}
