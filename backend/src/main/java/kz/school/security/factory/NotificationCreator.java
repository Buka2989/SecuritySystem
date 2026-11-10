package kz.school.security.factory;

import kz.school.security.entity.Notification;
import java.util.UUID;

/**
 * Factory Method: subclasses decide which concrete notification object to create.
 */
public abstract class NotificationCreator {
    public abstract Notification create(UUID recipientId, String title, String message);
}
