package com.swcampus.infra.postgres.post;

import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostRepository;
import com.swcampus.domain.post.PostSummary;
import com.swcampus.domain.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostEntityRepository implements PostRepository {

    private final PostJpaRepository jpaRepository;

    @Override
    public Post save(Post post) {
        PostEntity entity;

        if (post.getId() != null) {
            entity = jpaRepository.findById(post.getId())
                    .orElseThrow(() -> new PostNotFoundException(post.getId()));
            entity.update(post);
        } else {
            entity = PostEntity.from(post);
        }

        PostEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jpaRepository.findByIdAndNotDeleted(id)
                .map(PostEntity::toDomain);
    }

    @Override
    public Page<Post> findAll(List<Long> categoryIds, List<String> tags, Pageable pageable) {
        Long[] categoryIdsArray = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds.toArray(new Long[0]) : null;
        String[] tagsArray = (tags != null && !tags.isEmpty()) ? tags.toArray(new String[0]) : null;
        return jpaRepository.findAllWithFilters(categoryIdsArray, tagsArray, pageable)
                .map(PostEntity::toDomain);
    }

    @Override
    public void incrementViewCount(Long id) {
        jpaRepository.incrementViewCount(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Page<PostSummary> findAllWithDetails(List<Long> categoryIds, List<String> tags, String keyword, Pageable pageable) {
        Long[] categoryIdsArray = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds.toArray(new Long[0]) : null;
        String[] tagsArray = (tags != null && !tags.isEmpty()) ? tags.toArray(new String[0]) : null;
        
        Page<Object[]> results = jpaRepository.findAllWithDetails(categoryIdsArray, tagsArray, keyword, pageable);
        
        return results.map(row -> {
            Post post = mapRowToPost(row);
            String authorNickname = (String) row[13];
            String categoryName = (String) row[14];
            
            return PostSummary.builder()
                    .post(post)
                    .authorNickname(authorNickname)
                    .categoryName(categoryName)
                    .build();
        });
    }

    private Post mapRowToPost(Object[] row) {
        // Native Query 결과 매핑
        // 0: post_id, 1: board_category_id, 2: user_id, 3: post_title, 4: post_body
        // 5: post_images, 6: tags, 7: view_count, 8: like_count, 9: selected_comment_id
        // 10: is_deleted, 11: created_at, 12: updated_at, 13: author_nickname, 14: category_name
        
        Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
        Long boardCategoryId = row[1] != null ? ((Number) row[1]).longValue() : null;
        Long userId = row[2] != null ? ((Number) row[2]).longValue() : null;
        String title = (String) row[3];
        String body = (String) row[4];
        
        // PostgreSQL text[] 타입 처리
        List<String> images = parseStringArray(row[5]);
        List<String> tags = parseStringArray(row[6]);
        
        Long viewCount = row[7] != null ? ((Number) row[7]).longValue() : 0L;
        Long likeCount = row[8] != null ? ((Number) row[8]).longValue() : 0L;
        Long selectedCommentId = row[9] != null ? ((Number) row[9]).longValue() : null;
        boolean deleted = row[10] != null && (Boolean) row[10];
        
        java.time.LocalDateTime createdAt = resolveLocalDateTime(row[11]);
        java.time.LocalDateTime updatedAt = resolveLocalDateTime(row[12]);
        
        return Post.of(id, boardCategoryId, userId, title, body, images, tags,
                viewCount, likeCount, selectedCommentId, deleted, createdAt, updatedAt);
    }

    private java.time.LocalDateTime resolveLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof java.time.Instant) {
            return java.time.LocalDateTime.ofInstant((java.time.Instant) obj, java.time.ZoneId.systemDefault());
        }
        if (obj instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) obj;
        }
        return null; // or throw exception? For now null is safer to avoid crashing if unknown type, but better to log or fallback.
    }


    private List<String> parseStringArray(Object arrayObj) {
        if (arrayObj == null) {
            return new java.util.ArrayList<>();
        }
        if (arrayObj instanceof String[]) {
            return java.util.Arrays.asList((String[]) arrayObj);
        }
        if (arrayObj instanceof java.sql.Array) {
            try {
                Object array = ((java.sql.Array) arrayObj).getArray();
                if (array instanceof String[]) {
                    return java.util.Arrays.asList((String[]) array);
                }
            } catch (java.sql.SQLException e) {
                return new java.util.ArrayList<>();
            }
        }
        return new java.util.ArrayList<>();
    }
}
