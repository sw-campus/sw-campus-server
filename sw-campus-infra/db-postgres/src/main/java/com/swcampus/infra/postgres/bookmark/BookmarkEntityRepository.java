package com.swcampus.infra.postgres.bookmark;

import com.swcampus.domain.bookmark.Bookmark;
import com.swcampus.domain.bookmark.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class BookmarkEntityRepository implements BookmarkRepository {

    private final BookmarkJpaRepository jpaRepository;

    @Override
    public Bookmark save(Bookmark bookmark) {
        BookmarkEntity entity = BookmarkEntity.from(bookmark);
        BookmarkEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndPostId(Long userId, Long postId) {
        jpaRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public boolean existsByUserIdAndPostId(Long userId, Long postId) {
        return jpaRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public List<Bookmark> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(BookmarkEntity::toDomain)
                .toList();
    }

    @Override
    public Set<Long> findPostIdsByUserId(Long userId) {
        return new HashSet<>(jpaRepository.findPostIdsByUserId(userId));
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
