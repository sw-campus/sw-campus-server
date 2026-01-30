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
    public void incrementCommentCount(Long id) {
        jpaRepository.incrementCommentCount(id);
    }

    @Override
    public void decrementCommentCount(Long id) {
        jpaRepository.decrementCommentCount(id);
    }

    @Override
    public void incrementLikeCount(Long id) {
        jpaRepository.incrementLikeCount(id);
    }

    @Override
    public void decrementLikeCount(Long id) {
        jpaRepository.decrementLikeCount(id);
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
            String authorNickname = row[15] != null ? (String) row[15] : "알 수 없음";
            String categoryName = (String) row[16];
            
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
        // 5: post_images, 6: tags, 7: view_count, 8: like_count, 9: comment_count
        // 10: selected_comment_id, 11: is_deleted, 12: created_at, 13: updated_at
        // 14: is_pinned, 15: author_nickname, 16: category_name
        
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
        Long commentCount = row[9] != null ? ((Number) row[9]).longValue() : 0L;
        Long selectedCommentId = row[10] != null ? ((Number) row[10]).longValue() : null;
        boolean deleted = row[11] != null && (Boolean) row[11];
        
        java.time.LocalDateTime createdAt = resolveLocalDateTime(row[12]);
        java.time.LocalDateTime updatedAt = resolveLocalDateTime(row[13]);
        
        // is_pinned는 인덱스 14에 있음
        boolean pinned = row[14] != null && (Boolean) row[14];
        
        return Post.of(id, boardCategoryId, userId, title, body, images, tags,
                viewCount, likeCount, commentCount, selectedCommentId, pinned, deleted, createdAt, updatedAt);
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
            return java.util.List.of();
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
                return java.util.List.of();
            }
        }
        return java.util.List.of();
    }

    @Override
    public Optional<PostSummary> findPreviousPost(Long currentPostId) {
        java.util.List<Object[]> results = jpaRepository.findPreviousPostWithDetails(currentPostId);
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        return mapRowToPostSummary(results.get(0));
    }

    @Override
    public Optional<PostSummary> findNextPost(Long currentPostId) {
        java.util.List<Object[]> results = jpaRepository.findNextPostWithDetails(currentPostId);
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        return mapRowToPostSummary(results.get(0));
    }

    private Optional<PostSummary> mapRowToPostSummary(Object[] row) {
        if (row == null || row.length == 0 || row[0] == null) {
            return Optional.empty();
        }
        
        Post post = mapRowToPost(row);
        String authorNickname = row[15] != null ? (String) row[15] : "알 수 없음";
        String categoryName = (String) row[16];
        
        return Optional.of(PostSummary.builder()
                .post(post)
                .authorNickname(authorNickname)
                .categoryName(categoryName)
                .build());
    }

    @Override
    public Page<PostSummary> findByUserId(Long userId, Pageable pageable) {
        Page<Object[]> results = jpaRepository.findByUserIdWithDetails(userId, pageable);
        
        return results.map(row -> {
            Post post = mapRowToPost(row);
            String authorNickname = row[15] != null ? (String) row[15] : "알 수 없음";
            String categoryName = (String) row[16];
            
            return PostSummary.builder()
                    .post(post)
                    .authorNickname(authorNickname)
                    .categoryName(categoryName)
                    .build();
        });
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserIdNotDeleted(userId);
    }

    @Override
    public List<PostSummary> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.List.of();
        }

        Long[] idsArray = ids.toArray(new Long[0]);
        java.util.List<Object[]> results = jpaRepository.findAllByIdsWithDetails(idsArray);

        return results.stream()
                .map(row -> {
                    Post post = mapRowToPost(row);
                    String authorNickname = row[15] != null ? (String) row[15] : "알 수 없음";
                    String categoryName = (String) row[16];

                    return PostSummary.builder()
                            .post(post)
                            .authorNickname(authorNickname)
                            .categoryName(categoryName)
                            .build();
                })
                .toList();
    }

    @Override
    public Page<PostSummary> findCommentedByUserId(Long userId, Pageable pageable) {
        Page<Object[]> results = jpaRepository.findCommentedByUserIdWithDetails(userId, pageable);

        return results.map(row -> {
            Post post = mapRowToPost(row);
            String authorNickname = row[15] != null ? (String) row[15] : "알 수 없음";
            String categoryName = (String) row[16];

            return PostSummary.builder()
                    .post(post)
                    .authorNickname(authorNickname)
                    .categoryName(categoryName)
                    .build();
        });
    }

    @Override
    public long countCommentedByUserId(Long userId) {
        return jpaRepository.countCommentedByUserIdNotDeleted(userId);
    }

    @Override
    public void setUserIdNullByUserId(Long userId) {
        jpaRepository.setUserIdNullByUserId(userId);
    }

    @Override
    public java.util.Map<Long, String> findTitlesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return jpaRepository.findTitlesByIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));
    }
}
