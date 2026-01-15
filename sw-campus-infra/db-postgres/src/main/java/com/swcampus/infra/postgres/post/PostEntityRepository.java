package com.swcampus.infra.postgres.post;

import com.swcampus.domain.post.Post;
import com.swcampus.domain.post.PostRepository;
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
    public Page<Post> findAll(Long categoryId, List<String> tags, Pageable pageable) {
        String[] tagsArray = (tags != null && !tags.isEmpty()) ? tags.toArray(new String[0]) : null;
        return jpaRepository.findAllWithFilters(categoryId, tagsArray, pageable)
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
    public long countByPostId(Long postId) {
        return jpaRepository.countByPostId(postId);
    }
}
