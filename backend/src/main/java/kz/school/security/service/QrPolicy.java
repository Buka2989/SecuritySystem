package kz.school.security.service;
import java.time.LocalDateTime;
import kz.school.security.entity.*;
/** Pure decision logic. Caller must hold database locks before applying the decision. */
public final class QrPolicy {
    private QrPolicy() {}
    public static String rejection(QrCode q, GuardianStudent g, User guardian, Student student, LocalDateTime now) {
        if ("USED".equals(q.status)) return "QR_ALREADY_USED";
        if ("REVOKED".equals(q.status)) return "QR_REVOKED";
        if ("EXPIRED".equals(q.status) || !q.expiresAt.isAfter(now)) return "QR_EXPIRED";
        if (!"ACTIVE".equals(q.status)) return "INVALID_QR";
        if (g==null || !g.isActive || !"APPROVED".equals(g.verificationStatus)
            || guardian==null || !"ACTIVE".equals(guardian.status)
            || student==null || !"ACTIVE".equals(student.status)) return "NOT_VERIFIED";
        return null;
    }
}
