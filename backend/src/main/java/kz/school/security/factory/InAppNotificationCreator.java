package kz.school.security.factory;

import kz.school.security.entity.Notification;
import java.time.LocalDateTime;
import java.util.UUID;

/** Concrete Creator for notifications displayed inside the web application. */
public class InAppNotificationCreator extends NotificationCreator {
    @Override
    public Notification create(UUID recipientId, String title, String message) {
        Notification notification = new Notification();
        notification.recipientUserId = recipientId;
        notification.title = title;
        notification.message = message;
        notification.type = "IN_APP";
        notification.isRead = false;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }
}
