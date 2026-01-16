package com.swcampus.infra.postgres.postlike;

import com.swcampus.domain.postlike.PostLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_likes_seq")
    @SequenceGenerator(name = "post_likes_seq", sequenceName = "post_likes_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static PostLikeEntity from(PostLike postLike) {
        PostLikeEntity entity = new PostLikeEntity();
        entity.id = postLike.getId();
        entity.userId = postLike.getUserId();
        entity.postId = postLike.getPostId();
        entity.createdAt = postLike.getCreatedAt();
        return entity;
    }

    public PostLike toDomain() {
        return PostLike.of(
                this.id,
                this.userId,
                this.postId,
                this.createdAt
        );
    }
}
