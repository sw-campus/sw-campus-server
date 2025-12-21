package com.swcampus.api.member;

import com.swcampus.api.member.response.NicknameAvailableResponse;
import com.swcampus.domain.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @ApiResponse(responseCode = "200", description = "검사 완료")
    })
    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameAvailableResponse> checkNicknameAvailable(
            @Parameter(description = "확인할 닉네임", example = "길동이", required = true)
            @RequestParam("nickname") String nickname
    ) {
        boolean available = memberService.isNicknameAvailable(nickname, null);
        return ResponseEntity.ok(NicknameAvailableResponse.of(available));
    }
}
