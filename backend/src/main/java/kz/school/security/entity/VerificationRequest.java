package kz.school.security.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="verification_requests")
public class VerificationRequest {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="guardian_student_id", nullable=false) public UUID guardianStudentId;
    @Column(name="status", nullable=false, length=20) public String status;
    @Column(name="comment", nullable=true, columnDefinition="text") public String comment;
    @Column(name="created_at", nullable=false) public LocalDateTime createdAt;
    @Column(name="reviewed_at", nullable=true) public LocalDateTime reviewedAt;
}
