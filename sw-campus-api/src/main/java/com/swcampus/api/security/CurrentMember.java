package com.swcampus.api.security;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자의 MemberPrincipal을 주입받는 커스텀 어노테이션.
 *
 * <p>Spring Security의 {@link AuthenticationPrincipal}을 래핑하여:
 * <ul>
 *   <li>Swagger(OpenAPI) 문서에서 자동으로 숨김 처리</li>
 *   <li>타입 안전성 보장 (errorOnInvalidType = true)</li>
 *   <li>도메인 친화적인 어노테이션 네이밍</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * @GetMapping("/me")
 * public ResponseEntity<MemberResponse> getMyInfo(@CurrentMember MemberPrincipal member) {
 *     return ResponseEntity.ok(memberService.getInfo(member.memberId()));
 * }
 * }</pre>
 *
 * @see com.swcampus.domain.auth.MemberPrincipal
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(errorOnInvalidType = true)
@Parameter(hidden = true)
public @interface CurrentMember {
}
