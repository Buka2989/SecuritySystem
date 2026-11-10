package kz.school.security.service;

import kz.school.security.entity.Notification;
import kz.school.security.factory.NotificationCreator;
import kz.school.security.repository.NotificationRepository;
import kz.school.security.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notifications;
    private final NotificationCreator creator;

    public NotificationService(NotificationRepository notifications, NotificationCreator creator) {
        this.notifications = notifications;
        this.creator = creator;
    }

    public Notification createInApp(UUID recipientId, String title, String message) {
        return notifications.save(creator.create(recipientId, title, message));
    }

    @Transactional(readOnly = true)
    public List<Notification> listForUser(UUID userId) {
        return notifications.findAllByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notifications.findById(notificationId)
            .filter(n -> n.recipientUserId.equals(userId))
            .orElseThrow(() -> ApiException.missing("Notification not found"));
        notification.isRead = true;
    }
}
