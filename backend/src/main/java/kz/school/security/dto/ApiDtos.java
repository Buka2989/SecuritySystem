package kz.school.security.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;
public final class ApiDtos {
    private ApiDtos() {}
    public record Login(@NotNull String username, @NotNull String password) {}
    public record Token(String accessToken, String tokenType) {}
    public record UserCreate(@NotBlank @Size(max=150) String fullName, @NotBlank @Size(max=50) String username,
        @NotBlank @Size(max=1024) String password, @NotNull @Pattern(regexp="PARENT|TRUSTED_PERSON|SECURITY|ADMIN") String role,
        @Pattern(regexp="ACTIVE|BLOCKED") String status) {}
    public record UserResponse(UUID id, String fullName, String username, String role, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record ProfileUpdate(@NotBlank @Size(max=150) String fullName) {}
    public record StatusUpdate(@NotNull @Pattern(regexp="ACTIVE|BLOCKED") String status) {}
    public record StudentCreate(@NotBlank @Size(max=150) String fullName, @NotBlank @Size(max=20) String className,
        @Pattern(regexp="ACTIVE|INACTIVE") String status) {}
    public record StudentResponse(UUID id, String fullName, String className, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record LinkCreate(@NotNull UUID studentId, @NotNull @Pattern(regexp="FATHER|MOTHER|GRANDFATHER|GRANDMOTHER|GUARDIAN") String relationshipType) {}
    public record LinkResponse(UUID id, UUID studentId, UUID guardianId, String relationshipType, String verificationStatus, LocalDateTime createdAt) {}
    public record VerificationCreate(@NotNull UUID guardianStudentId) {}
    public record Reject(@NotNull String comment) {}
    public record VerificationResponse(UUID id, UUID guardianStudentId, String status, String comment, LocalDateTime createdAt, LocalDateTime reviewedAt) {}
    public record AdminVerificationResponse(UUID id, UUID guardianStudentId, String guardianName, String studentName, String className,
        String relationshipType, String status, String comment, LocalDateTime createdAt, LocalDateTime reviewedAt) {}
    public record QrCreate(@NotNull UUID guardianStudentId, @Min(1) @Max(1440) Integer expiresInMinutes) {}
    public record QrResponse(UUID id, UUID guardianStudentId, String token, LocalDateTime expiresAt, String status, LocalDateTime createdAt, LocalDateTime usedAt) {}
    public record QrVerify(@NotNull @Size(max=4096) String token) {}
    public record VerifyResponse(String result, String reason, String guardianName, String studentName, String className, LocalDateTime verifiedAt) {
        public static VerifyResponse denied(String reason) { return new VerifyResponse("DENIED",reason,null,null,null,null); }
    }
    public record LogResponse(UUID id, UUID qrCodeId, UUID securityUserId, String action, String result, String reason, LocalDateTime createdAt) {}
}
