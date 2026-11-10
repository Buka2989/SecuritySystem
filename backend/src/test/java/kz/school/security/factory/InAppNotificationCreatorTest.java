package kz.school.security.factory;

import kz.school.security.entity.Notification;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InAppNotificationCreatorTest {
    @Test
    void createsUnreadInAppNotificationForRecipient() {
        UUID recipient = UUID.randomUUID();
        Notification notification = new InAppNotificationCreator()
            .create(recipient, "Заявка одобрена", "Ваша заявка одобрена.");

        assertEquals(recipient, notification.recipientUserId);
        assertEquals("Заявка одобрена", notification.title);
        assertEquals("Ваша заявка одобрена.", notification.message);
        assertEquals("IN_APP", notification.type);
        assertFalse(notification.isRead);
        assertNotNull(notification.createdAt);
    }
}
