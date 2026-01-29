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
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SseEmitterService {

    private static final Long TIMEOUT = 30 * 60 * 1000L; // 30분
    private static final long HEARTBEAT_INTERVAL = 30L; // 30초마다 heartbeat
    private final Map<Long, EmitterWrapper> emitters = new ConcurrentHashMap<>();
    private ScheduledExecutorService heartbeatScheduler;

    private record EmitterWrapper(SseEmitter emitter, AtomicBoolean completed) {
        static EmitterWrapper of(SseEmitter emitter) {
            return new EmitterWrapper(emitter, new AtomicBoolean(false));
        }

        void markCompleted() {
            completed.set(true);
        }

        boolean isCompleted() {
            return completed.get();
        }

        boolean trySend(SseEmitter.SseEventBuilder event) throws IOException {
            if (isCompleted()) {
                return false;
            }
            emitter.send(event);
            return true;
        }
    }

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
        emitters.forEach((userId, wrapper) -> {
            try {
                if (!wrapper.trySend(SseEmitter.event().comment("heartbeat"))) {
                    emitters.remove(userId, wrapper);
                }
            } catch (Exception e) {
                log.debug("Heartbeat failed for user: {}, removing emitter", userId);
                wrapper.markCompleted();
                emitters.remove(userId, wrapper);
            }
        });
    }

    public SseEmitter createEmitter(Long userId) {
        // 기존 연결이 있으면 종료 (콜백이 새 emitter를 제거하지 않도록 먼저 map에서 제거)
        EmitterWrapper oldWrapper = emitters.remove(userId);
        if (oldWrapper != null) {
            oldWrapper.markCompleted();
            try {
                oldWrapper.emitter().complete();
            } catch (Exception ignored) {
                // 이미 완료된 emitter일 수 있음
            }
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        EmitterWrapper wrapper = EmitterWrapper.of(emitter);
        emitters.put(userId, wrapper);

        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for user: {}", userId);
            wrapper.markCompleted();
            // 현재 emitter와 동일한 경우에만 제거 (경쟁 조건 방지)
            emitters.remove(userId, wrapper);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE connection timeout for user: {}", userId);
            wrapper.markCompleted();
            emitters.remove(userId, wrapper);
        });

        emitter.onError(e -> {
            log.debug("SSE connection error for user: {}", userId);
            wrapper.markCompleted();
            emitters.remove(userId, wrapper);
        });

        // 연결 성공 이벤트 전송
        try {
            if (!wrapper.trySend(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected"))) {
                log.debug("Emitter already completed for user: {}", userId);
                emitters.remove(userId, wrapper);
            }
        } catch (IOException e) {
            log.debug("Failed to send connect event to user: {}", userId);
            wrapper.markCompleted();
            emitters.remove(userId, wrapper);
        }

        return emitter;
    }

    public void sendNotification(Long userId, Notification notification, String senderNickname, Long postId) {
        EmitterWrapper wrapper = emitters.get(userId);
        if (wrapper == null) {
            return;
        }

        try {
            if (!wrapper.trySend(SseEmitter.event()
                    .name("notification")
                    .data(NotificationResponse.from(notification, senderNickname, postId)))) {
                log.debug("Emitter already completed for user: {}", userId);
                emitters.remove(userId, wrapper);
            }
        } catch (IOException e) {
            log.debug("Failed to send notification to user: {}", userId);
            wrapper.markCompleted();
            emitters.remove(userId, wrapper);
        }
    }

    public void removeEmitter(Long userId) {
        EmitterWrapper wrapper = emitters.remove(userId);
        if (wrapper != null) {
            wrapper.markCompleted();
            try {
                wrapper.emitter().complete();
            } catch (Exception ignored) {
                // 이미 완료된 emitter일 수 있음
            }
        }
    }

    public boolean hasConnection(Long userId) {
        return emitters.containsKey(userId);
    }
}
