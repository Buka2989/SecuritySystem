package kz.school.security.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="guardian_student")
public class GuardianStudent {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="student_id", nullable=false) public UUID studentId;
    @Column(name="guardian_id", nullable=false) public UUID guardianId;
    @Column(name="relationship_type", nullable=false, length=30) public String relationshipType;
    @Column(name="verification_status", nullable=false, length=20) public String verificationStatus;
    @Column(name="created_at", nullable=false) public LocalDateTime createdAt;
    @Column(name="is_active", nullable=false) public boolean isActive;
}
