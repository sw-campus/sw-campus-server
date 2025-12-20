package com.swcampus.shared.error;

/**
 * 클라이언트/전송 수준 예외를 게이트웨이 HTTP 상태 코드로 매핑
 */
public final class GatewayStatusInferrer {
    private GatewayStatusInferrer() {}

    public static int inferFromMessage(String message) {
        if (message == null) return 502;
        String lower = message.toLowerCase();
        if (lower.contains("timed out") || lower.contains("timeout") || lower.contains("read timed out") || lower.contains("connect timed out")) {
            return 504; // Gateway Timeout
        }
        if (lower.contains("connection refused") || lower.contains("connection reset") || lower.contains("unresolved") || lower.contains("broken pipe")) {
            return 502; // Bad Gateway
        }
        if (lower.contains("service unavailable") || lower.contains("throttling") || lower.contains("slowdown") || lower.contains("rate exceeded")) {
            return 503; // Service Unavailable
        }
        return 502;
    }
}
