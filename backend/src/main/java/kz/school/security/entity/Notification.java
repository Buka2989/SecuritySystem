package kz.school.security.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    public UUID id = UUID.randomUUID();

    @Column(name = "recipient_user_id", nullable = false)
    public UUID recipientUserId;

    @Column(name = "title", nullable = false, length = 150)
    public String title;

    @Column(name = "message", nullable = false, length = 1000)
    public String message;

    @Column(name = "type", nullable = false, length = 40)
    public String type;

    @Column(name = "is_read", nullable = false)
    public boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
