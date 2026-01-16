package com.swcampus.api.security;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자의 MemberPrincipal을 선택적으로 주입받는 커스텀 어노테이션.
 *
 * <p>{@link CurrentMember}와 달리, 인증되지 않은 사용자도 접근 가능한 엔드포인트에서 사용:
 * <ul>
 *   <li>인증된 경우: MemberPrincipal 반환</li>
 *   <li>미인증된 경우: null 반환 (에러 발생하지 않음)</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * @GetMapping("/{id}")
 * public ResponseEntity<ReviewResponse> getReview(
 *         @OptionalCurrentMember MemberPrincipal member,
 *         @PathVariable Long id) {
 *     Long requesterId = member != null ? member.memberId() : null;
 *     return ResponseEntity.ok(reviewService.getReview(id, requesterId));
 * }
 * }</pre>
 *
 * @see CurrentMember
 * @see com.swcampus.domain.auth.MemberPrincipal
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this")
@Parameter(hidden = true)
public @interface OptionalCurrentMember {
}
