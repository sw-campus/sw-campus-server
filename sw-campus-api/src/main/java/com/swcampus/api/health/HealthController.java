package com.swcampus.api.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    // ✅ ALB 헬스체크 전용 엔드포인트 (가장 정석적인 해결)
    // - Spring Security에서 permitAll()로 완전 공개
    // - JWT 필터보다 앞에서 처리됨
    // - ALB 헬스체크 경로로 사용
    @GetMapping("/healthz")
    public ResponseEntity<String> healthz() {
        return ResponseEntity.ok("OK");
    }
}

