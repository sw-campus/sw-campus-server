package com.swcampus.api.notification;

import com.swcampus.domain.notification.Notification;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SseEmitterService {

    private static final Long TIMEOUT = 30 * 60 * 1000L; // 30분
    private static final long HEARTBEAT_INTERVAL = 30L; // 30초마다 heartbeat
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private ScheduledExecutorService heartbeatScheduler;

    @PostConstruct
    public void init() {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeatToAll,
                HEARTBEAT_INTERVAL,
                HEARTBEAT_INTERVAL,
                TimeUnit.SECONDS
        );
        log.info("SSE heartbeat scheduler started with {}s interval", HEARTBEAT_INTERVAL);
    }

    @PreDestroy
    public void destroy() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
        }
    }

    private void sendHeartbeatToAll() {
        // 순회 중 제거를 위해 복사본으로 순회
        emitters.keySet().forEach(userId -> {
            SseEmitter emitter = emitters.get(userId);
            if (emitter == null) {
                return;
            }
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                log.debug("Heartbeat failed for user: {}, removing emitter", userId);
                emitters.remove(userId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // 이미 완료된 emitter일 수 있음
                }
            }
        });
    }

    public SseEmitter createEmitter(Long userId) {
        // 기존 연결이 있으면 종료
        if (emitters.containsKey(userId)) {
            emitters.get(userId).complete();
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for user: {}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE connection timeout for user: {}", userId);
            emitter.complete();
            emitters.remove(userId);
        });

        emitter.onError(e -> {
            log.debug("SSE connection error for user: {}", userId);
            emitters.remove(userId);
        });

        // 연결 성공 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected"));
        } catch (IOException e) {
            log.error("Failed to send connect event to user: {}", userId, e);
            emitters.remove(userId);
        }

        return emitter;
    }

    public void sendNotification(Long userId, Notification notification, String senderNickname, Long postId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(NotificationResponse.from(notification, senderNickname, postId)));
        } catch (IOException e) {
            log.error("Failed to send notification to user: {}", userId, e);
            emitters.remove(userId);
        }
    }

    public void removeEmitter(Long userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            emitter.complete();
            emitters.remove(userId);
        }
    }

    public boolean hasConnection(Long userId) {
        return emitters.containsKey(userId);
    }
}
