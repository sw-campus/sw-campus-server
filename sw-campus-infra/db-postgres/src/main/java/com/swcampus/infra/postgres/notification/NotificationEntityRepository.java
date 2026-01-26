package com.swcampus.infra.postgres.notification;

import com.swcampus.domain.notification.Notification;
import com.swcampus.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationEntityRepository implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity;

        if (notification.getId() != null) {
            entity = jpaRepository.findById(notification.getId())
                    .orElse(NotificationEntity.from(notification));
            entity.update(notification);
        } else {
            entity = NotificationEntity.from(notification);
        }

        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByUserIdAndReadFalse(Long userId) {
        return jpaRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndReadFalse(Long userId) {
        return jpaRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markAllAsReadByUserId(Long userId) {
        jpaRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
