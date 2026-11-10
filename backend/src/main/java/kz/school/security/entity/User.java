package kz.school.security.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="users")
public class User {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="full_name", nullable=false, length=150) public String fullName;
    @Column(name="username", nullable=false, length=50) public String username;
    @Column(name="password_hash", nullable=false, length=255) public String passwordHash;
    @Column(name="role", nullable=false, length=20) public String role;
    @Column(name="status", nullable=false, length=20) public String status;
    @Column(name="created_at", nullable=false) public LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false) public LocalDateTime updatedAt;
}
