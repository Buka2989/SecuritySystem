package kz.school.security.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="qr_codes")
public class QrCode {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="guardian_student_id", nullable=false) public UUID guardianStudentId;
    @Column(name="token", nullable=false, length=255) public String token;
    @Column(name="expires_at", nullable=false) public LocalDateTime expiresAt;
    @Column(name="status", nullable=false, length=20) public String status;
    @Column(name="created_at", nullable=false) public LocalDateTime createdAt;
    @Column(name="used_at", nullable=true) public LocalDateTime usedAt;
}
