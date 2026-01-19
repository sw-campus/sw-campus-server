# 07. Swagger(OpenAPI) 문서화 규칙

> Springdoc OpenAPI를 사용하여 API 문서를 자동 생성하고 관리합니다.

---

## 📦 의존성 설정

### build.gradle (api 모듈)

```gradle
dependencies {
    // Swagger UI + OpenAPI 3.0
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13'
}
```

---

## 🏷️ Controller 문서화

### 기본 패턴

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "사용자 상세 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "사용자 ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "사용자 삭제")
    @SecurityRequirement(name = "cookieAuth")  // 인증 필요 표시
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        // ...
    }
}
```

---

## 📤 Multipart 파일 업로드 처리

> **중요**: `@ModelAttribute`와 `MultipartFile`을 함께 사용하면 Swagger UI에서 파일 업로드 필드가 표시되지 않습니다.

### ❌ 잘못된 패턴

```java
// @ModelAttribute + MultipartFile 조합은 Swagger에서 제대로 동작하지 않음
@PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<Response> signup(
        @Valid @ModelAttribute SignupRequest request,  // ❌ 파일 필드가 표시되지 않음
        @RequestParam("image") MultipartFile image) {
    // ...
}
```

### ✅ 올바른 패턴 (@RequestPart 사용)

```java
@PostMapping(value = "/signup/organization", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "기관 회원가입", description = "기관 사용자로 회원가입합니다.")
public ResponseEntity<SignupResponse> signupOrganization(
        @Parameter(description = "이메일", example = "org@example.com", required = true)
        @RequestPart(name = "email") String email,

        @Parameter(description = "비밀번호 (8자 이상)", example = "Password123!", required = true)
        @RequestPart(name = "password") String password,

        @Parameter(description = "재직증명서 이미지 (jpg, png)", required = true)
        @RequestPart(name = "certificateImage") MultipartFile certificateImage
) throws IOException {
    // Controller 내부에서 Request DTO 생성
    SignupRequest request = SignupRequest.builder()
            .email(email)
            .password(password)
            .certificateImage(certificateImage)
            .build();

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.signup(request.toCommand()));
}
```

---

## 🔷 JSON 문자열 + 파일 업로드 (복합 데이터)

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Operation(summary = "강의 등록")
public ResponseEntity<LectureResponse> createLecture(
        @CurrentMember MemberPrincipal member,

        // ✅ 핵심: schema 속성으로 JSON 구조를 Swagger에서 표시
        @Parameter(
            description = "강의 정보 (JSON string)",
            schema = @io.swagger.v3.oas.annotations.media.Schema(
                implementation = LectureCreateRequest.class
            )
        )
        @RequestPart("lecture") String lectureJson,

        @Parameter(description = "강의 대표 이미지 파일")
        @RequestPart(value = "image", required = false) MultipartFile image
) throws IOException {

    // JSON 파싱
    LectureCreateRequest request = objectMapper.readValue(lectureJson, LectureCreateRequest.class);

    // 수동 유효성 검증 (@Valid가 @RequestPart String에 동작하지 않으므로)
    Set<ConstraintViolation<LectureCreateRequest>> violations = validator.validate(request);
    if (!violations.isEmpty()) {
        throw new ConstraintViolationException(violations);
    }

    // ...
}
```

**주의사항:**
- `@RequestPart`로 받은 JSON 문자열에는 `@Valid`가 동작하지 않음
- 반드시 `Validator`를 주입받아 수동 검증 필요

---

## ⚠️ 에러 응답 문서화 (중요)

> **필수**: 모든 에러 응답(400, 401, 403, 404, 409 등)에는 반드시 `content`와 `examples`를 추가해야 합니다.

### ✅ 올바른 패턴

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "401", description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {"status": 401, "message": "인증이 필요합니다", "timestamp": "2025-12-09T12:00:00"}
                """))),
    @ApiResponse(responseCode = "403", description = "권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {"status": 403, "message": "접근 권한이 없습니다", "timestamp": "2025-12-09T12:00:00"}
                """)))
})
```

---

## 🔒 인증 API 표시

### Controller 전체가 인증 필요한 경우 (Class-level)

```java
@RestController
@RequestMapping("/api/v1/mypage")
@Tag(name = "마이페이지", description = "마이페이지 관련 API")
@SecurityRequirement(name = "cookieAuth")  // ✅ 클래스 레벨에 선언
public class MypageController {
    // 모든 메서드에 자동 적용됨
}
```

### 일부 메서드만 인증 필요한 경우 (Method-level)

```java
@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    // 인증 불필요
    @GetMapping("/{lectureId}")
    @Operation(summary = "강의 리뷰 목록 조회")
    public ResponseEntity<List<ReviewResponse>> getReviews(...) { }

    // ✅ 인증 필요 (메서드 레벨)
    @PostMapping
    @Operation(summary = "리뷰 작성")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<ReviewResponse> createReview(...) { }
}
```

---

## Multipart 처리 규칙 요약

| 항목 | 규칙 |
|------|------|
| 파일 + 텍스트 필드 | `@RequestPart`로 각 필드 분리 |
| Content-Type | `MediaType.MULTIPART_FORM_DATA_VALUE` 명시 |
| 숫자 타입 | String으로 받아서 파싱 + **try-catch 필수** |
| JSON 문자열 | `schema = @Schema(implementation = ...)` 필수 |
| JSON 유효성 검증 | `Validator` 수동 검증 필수 |
| 선택적 파일 | `required = false` 명시 |

---

## 🚫 하지 말 것

| 금지 사항 | 이유 |
|----------|------|
| `@ModelAttribute` + `MultipartFile` | Swagger UI에서 파일 필드 표시 안됨 |
| JSON 문자열에 `schema` 속성 누락 | Swagger에서 JSON 구조 표시 안됨 |
| `@Valid` on `@RequestPart` String | 동작하지 않음, `Validator` 수동 검증 필요 |
| 인증 API에 `@SecurityRequirement` 누락 | 프론트엔드가 인증 필요 여부 알 수 없음 |
| description 없는 `@Operation` | 무의미한 문서 |

---

## ✅ 체크리스트

### Controller (필수)

- [ ] `@Tag`로 API 그룹 분류했는가?
- [ ] 모든 메서드에 `@Operation(summary = "...")` 있는가?
- [ ] 주요 응답 코드에 `@ApiResponse` 있는가?
- [ ] 인증 필요 API에 `@SecurityRequirement` 있는가?

### Multipart API (필수)

- [ ] `@RequestPart`로 각 필드를 분리했는가? (`@ModelAttribute` 금지)
- [ ] 모든 파라미터에 `@Parameter(description = "...")` 있는가?
- [ ] JSON 파싱 후 `Validator`로 수동 검증하는가?
