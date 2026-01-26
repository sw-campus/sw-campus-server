package com.swcampus.domain.postlike;

import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberRepository;
import com.swcampus.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 게시글 추천 토글 - 이미 추천했으면 취소, 없으면 추천
     * @return true: 추천 추가됨, false: 추천 취소됨
     */
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            postLikeRepository.deleteByUserIdAndPostId(userId, postId);
            postRepository.decrementLikeCount(postId);
            return false;
        } else {
            PostLike postLike = PostLike.create(userId, postId);
            postLikeRepository.save(postLike);
            postRepository.incrementLikeCount(postId);
            return true;
        }
    }

    /**
     * 특정 게시글 추천 여부 확인
     */
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null) {
            return false;
        }
        return postLikeRepository.existsByUserIdAndPostId(userId, postId);
    }

    /**
     * 게시글 추천 수 조회
     */
    public long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    /**
     * 사용자가 추천한 게시글 ID 목록 조회 (일괄 조회용)
     */
    public Set<Long> getLikedPostIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return postLikeRepository.findPostIdsByUserId(userId);
    }

    /**
     * 게시글에 좋아요 누른 사용자 ID 목록 조회
     */
    public List<Long> getLikerIds(Long postId) {
        return postLikeRepository.findUserIdsByPostId(postId);
    }

    /**
     * 게시글에 좋아요 누른 사용자 정보 목록 조회 (일괄 조회)
     * 탈퇴한 회원(userId가 NULL)은 "알 수 없음"으로 표시됩니다.
     */
    public List<LikerInfo> getLikers(Long postId) {
        List<Long> likerIds = postLikeRepository.findUserIdsByPostId(postId);

        if (likerIds.isEmpty()) {
            return List.of();
        }

        // 탈퇴한 회원의 null ID 제외
        List<Long> validIds = likerIds.stream()
                .filter(id -> id != null)
                .toList();

        Map<Long, Member> memberMap = validIds.isEmpty()
                ? Map.of()
                : memberRepository.findAllByIds(validIds).stream()
                        .collect(Collectors.toMap(Member::getId, Function.identity()));

        return likerIds.stream()
                .map(id -> {
                    if (id == null) {
                        return LikerInfo.of(null, "알 수 없음");
                    }
                    Member member = memberMap.get(id);
                    return member != null
                            ? LikerInfo.of(member.getId(), member.getNickname())
                            : LikerInfo.of(id, "알 수 없음");
                })
                .toList();
    }
}
