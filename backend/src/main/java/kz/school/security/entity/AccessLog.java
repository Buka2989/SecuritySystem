package kz.school.security.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="access_logs")
public class AccessLog {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="qr_code_id", nullable=false) public UUID qrCodeId;
    @Column(name="security_user_id", nullable=false) public UUID securityUserId;
    @Column(name="action", nullable=false, length=20) public String action;
    @Column(name="result", nullable=false, length=20) public String result;
    @Column(name="reason", nullable=false, length=50) public String reason;
    @Column(name="created_at", nullable=false) public LocalDateTime createdAt;
}
