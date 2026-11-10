package kz.school.security.repository;

import kz.school.security.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
}
