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

    // ❌ /healthz 엔드포인트 주석 처리 (actuator health 사용)
    // - ALB 헬스체크 경로: /actuator/health 사용
    // - SecurityConfig에서 /actuator/health/** permitAll()로 허용
    // - JwtAuthenticationFilter에서 shouldNotFilter로 제외
    // @GetMapping("/healthz")
    // public ResponseEntity<String> healthz() {
    //     return ResponseEntity.ok("OK");
    // }
}

