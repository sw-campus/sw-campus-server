package com.swcampus.infra.postgres.notification;

import com.swcampus.domain.notification.Notification;
import com.swcampus.domain.notification.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_seq")
    @SequenceGenerator(name = "notifications_seq", sequenceName = "notifications_noti_id_seq", allocationSize = 1)
    @Column(name = "noti_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static NotificationEntity from(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.id = notification.getId();
        entity.userId = notification.getUserId();
        entity.senderId = notification.getSenderId();
        entity.targetId = notification.getTargetId();
        entity.type = notification.getType();
        entity.read = notification.isRead();
        return entity;
    }

    public void update(Notification notification) {
        this.read = notification.isRead();
    }

    public Notification toDomain() {
        return Notification.of(
                this.id,
                this.userId,
                this.senderId,
                this.targetId,
                this.type,
                this.read,
                this.createdAt
        );
    }
}
