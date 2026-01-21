package com.swcampus.api.comment;

import com.swcampus.api.comment.response.CommentResponse;
import com.swcampus.domain.comment.Comment;
import com.swcampus.domain.commentlike.CommentLikeService;
import com.swcampus.domain.member.Member;
import com.swcampus.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 댓글 도메인 객체를 응답 DTO로 변환하는 매퍼 클래스.
 * 계층 구조 변환, 작성자 정보 조회, 추천 정보 추가 등의 복잡한 변환 로직을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class CommentResponseMapper {

    private final MemberService memberService;
    private final CommentLikeService commentLikeService;

    /**
     * 댓글 목록을 계층 구조(부모-자식)로 변환합니다.
     * N+1 문제를 해결하기 위해 작성자 정보를 일괄 조회합니다.
     *
     * @param comments 댓글 목록
     * @param currentUserId 현재 로그인한 사용자 ID (비로그인 시 null)
     * @return 계층 구조로 변환된 댓글 응답 목록
     */
    public List<CommentResponse> toTreeResponse(List<Comment> comments, Long currentUserId) {
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 0. 중복 제거 (DB에서 DISTINCT를 사용하더라도 안전장치로 추가)
        List<Comment> uniqueComments = comments.stream()
                .filter(distinctByKey(Comment::getId))
                .toList();

        // 1. 작성자 ID 목록 수집
        List<Long> authorIds = uniqueComments.stream()
                .map(Comment::getUserId)
                .distinct()
                .toList();

        // 2. 작성자 정보 일괄 조회
        Map<Long, String> nicknameMap = memberService.getMembersByIds(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        Map<Long, CommentResponse> commentMap = new LinkedHashMap<>();
        List<CommentResponse> rootComments = new ArrayList<>();

        // 3. 사용자가 추천한 댓글 ID 목록 조회
        Set<Long> likedCommentIds = commentLikeService.getLikedCommentIds(currentUserId);

        // 4. 모든 댓글을 CommentResponse로 변환하고 Map에 저장
        for (Comment comment : uniqueComments) {
            String nickname = nicknameMap.getOrDefault(comment.getUserId(), "알 수 없음");
            boolean isAuthor = currentUserId != null && comment.isAuthor(currentUserId);
            boolean isLiked = likedCommentIds.contains(comment.getId());
            CommentResponse response = CommentResponse.from(comment, nickname, isAuthor, isLiked);
            commentMap.put(comment.getId(), response);
        }

        // 5. 부모-자식 관계 설정
        for (Comment comment : uniqueComments) {
            CommentResponse response = commentMap.get(comment.getId());
            if (comment.getParentId() == null) {
                // 루트 댓글
                rootComments.add(response);
            } else {
                // 대댓글: 부모 댓글에 추가
                CommentResponse parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.addReply(response);
                } else {
                    // 부모가 삭제된 경우 루트로 표시
                    rootComments.add(response);
                }
            }
        }

        return rootComments;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
