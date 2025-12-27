package com.swcampus.api.cart;

import com.swcampus.api.cart.response.CartLectureResponse;
import com.swcampus.api.security.CurrentMember;
import com.swcampus.domain.auth.MemberPrincipal;
import com.swcampus.domain.cart.CartService;
import com.swcampus.domain.lecture.Lecture;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart", description = "장바구니 API")
@SecurityRequirement(name = "cookieAuth")
public class CartController {

    private final CartService cartService;

    @PostMapping
    @Operation(summary = "장바구니 추가", description = "강의를 장바구니에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 장바구니에 존재")
    })
    public ResponseEntity<Void> addCart(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "강의 ID", example = "1", required = true)
            @RequestParam(name = "lectureId") Long lectureId) {
        Long currentUserId = member.memberId();
        cartService.addCart(currentUserId, lectureId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(summary = "장바구니 삭제", description = "장바구니에서 강의를 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "장바구니에 해당 강의 없음")
    })
    public ResponseEntity<Void> removeCart(
            @CurrentMember MemberPrincipal member,
            @Parameter(description = "강의 ID", example = "1", required = true)
            @RequestParam(name = "lectureId") Long lectureId) {
        Long currentUserId = member.memberId();
        cartService.removeCart(currentUserId, lectureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "장바구니 목록 조회", description = "장바구니에 담긴 강의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<List<CartLectureResponse>> getCart(
            @CurrentMember MemberPrincipal member) {
        Long currentUserId = member.memberId();
        List<Lecture> lectures = cartService.getCartList(currentUserId);
        List<CartLectureResponse> response = lectures.stream()
                .map(CartLectureResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
