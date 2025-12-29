package com.swcampus.api.member;

import com.swcampus.api.member.response.NicknameAvailableResponse;
import com.swcampus.api.member.response.WithdrawResponse;
import com.swcampus.api.ratelimit.RateLimited;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.member.MemberService;
import com.swcampus.domain.oauth.OAuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "닉네임 중복 검사",
            description = "닉네임 사용 가능 여부를 확인합니다. 대소문자를 구분하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검사 완료"),
            @ApiResponse(responseCode = "429", description = "요청 횟수 초과 (분당 20회)")
    })
    @RateLimited(key = "nickname-check", limit = 20, windowSeconds = 60)
    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameAvailableResponse> checkNicknameAvailable(
            @Parameter(description = "확인할 닉네임", example = "길동이", required = true)
            @RequestParam("nickname") String nickname
    ) {
        boolean available = memberService.isNicknameAvailable(nickname, null);
        return ResponseEntity.ok(NicknameAvailableResponse.of(available));
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 회원을 탈퇴 처리합니다. 모든 관련 데이터가 삭제됩니다. 기관 회원 및 관리자는 탈퇴할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 완료"),
            @ApiResponse(responseCode = "400", description = "기관 회원 또는 관리자는 탈퇴 불가"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/me")
    public ResponseEntity<WithdrawResponse> withdraw(
            @CurrentMember MemberPrincipal member
    ) {
        List<OAuthProvider> providers = memberService.withdraw(member.memberId());
        List<String> providerNames = providers.stream()
                .map(OAuthProvider::name)
                .toList();
        return ResponseEntity.ok(WithdrawResponse.success(providerNames));
    }
}

