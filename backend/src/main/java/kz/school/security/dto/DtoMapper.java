package kz.school.security.dto;
import kz.school.security.entity.*;
import static kz.school.security.dto.ApiDtos.*;
public final class DtoMapper {
    private DtoMapper() {}
    public static UserResponse user(User u) { return new UserResponse(u.id,u.fullName,u.username,u.role,u.status,u.createdAt,u.updatedAt); }
    public static StudentResponse student(Student s) { return new StudentResponse(s.id,s.fullName,s.className,s.status,s.createdAt,s.updatedAt); }
    public static LinkResponse link(GuardianStudent g) { return new LinkResponse(g.id,g.studentId,g.guardianId,g.relationshipType,g.verificationStatus,g.createdAt); }
    public static VerificationResponse verification(VerificationRequest v) { return new VerificationResponse(v.id,v.guardianStudentId,v.status,v.comment,v.createdAt,v.reviewedAt); }
    public static QrResponse qr(QrCode q) { return new QrResponse(q.id,q.guardianStudentId,q.token,q.expiresAt,q.status,q.createdAt,q.usedAt); }
    public static LogResponse log(AccessLog l) { return new LogResponse(l.id,l.qrCodeId,l.securityUserId,l.action,l.result,l.reason,l.createdAt); }
}
