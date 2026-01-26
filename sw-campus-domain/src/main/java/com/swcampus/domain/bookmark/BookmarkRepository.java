package com.swcampus.domain.bookmark;

import java.util.List;
import java.util.Set;

public interface BookmarkRepository {

    Bookmark save(Bookmark bookmark);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    List<Bookmark> findByUserId(Long userId);

    Set<Long> findPostIdsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
