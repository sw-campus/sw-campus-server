package com.swcampus.infra.postgres.bookmark;

import com.swcampus.domain.bookmark.Bookmark;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookmarks_seq")
    @SequenceGenerator(name = "bookmarks_seq", sequenceName = "bookmarks_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static BookmarkEntity from(Bookmark bookmark) {
        BookmarkEntity entity = new BookmarkEntity();
        entity.id = bookmark.getId();
        entity.userId = bookmark.getUserId();
        entity.postId = bookmark.getPostId();
        entity.createdAt = bookmark.getCreatedAt();
        return entity;
    }

    public Bookmark toDomain() {
        return Bookmark.of(
                this.id,
                this.userId,
                this.postId,
                this.createdAt
        );
    }
}
