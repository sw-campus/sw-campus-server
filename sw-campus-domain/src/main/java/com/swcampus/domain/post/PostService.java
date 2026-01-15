package com.swcampus.domain.post;

import com.swcampus.domain.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Post createPost(Long userId, Long boardCategoryId, String title, String body,
                           List<String> images, List<String> tags) {
        Post post = Post.create(boardCategoryId, userId, title, body, images, tags);
        return postRepository.save(post);
    }

    public Page<Post> getPosts(Long categoryId, List<String> tags, Pageable pageable) {
        return postRepository.findAll(categoryId, tags, pageable);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    @Transactional
    public Post getPostWithViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        postRepository.incrementViewCount(postId);
        post.incrementViewCount();
        
        return post;
    }

    @Transactional
    public Post updatePost(Long postId, Long userId, String title, String body,
                           List<String> images, List<String> tags) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.isAuthor(userId)) {
            throw new IllegalStateException("게시글 수정 권한이 없습니다.");
        }

        post.update(title, body, images, tags);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.isAuthor(userId)) {
            throw new IllegalStateException("게시글 삭제 권한이 없습니다.");
        }

        post.delete();
        postRepository.save(post);
    }

    @Transactional
    public void selectComment(Long postId, Long userId, Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.isAuthor(userId)) {
            throw new IllegalStateException("답변 채택 권한이 없습니다.");
        }

        post.selectComment(commentId);
        postRepository.save(post);
    }
}
