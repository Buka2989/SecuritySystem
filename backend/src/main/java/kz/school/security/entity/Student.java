package kz.school.security.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="students")
public class Student {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="full_name", nullable=false, length=150) public String fullName;
    @Column(name="class_name", nullable=false, length=20) public String className;
    @Column(name="status", nullable=false, length=20) public String status;
    @Column(name="created_at", nullable=false) public LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) public LocalDateTime updatedAt;
}
