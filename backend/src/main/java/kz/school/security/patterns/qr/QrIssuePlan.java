package kz.school.security.patterns.qr;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import kz.school.security.entity.QrCode;

/** Builder collects independent issue options and validates them before constructing a QR. */
public final class QrIssuePlan {
    private final UUID relationshipId;
    private final LocalDateTime issuedAt;
    private final int lifetimeMinutes;
    private QrIssuePlan(Builder b) {
        relationshipId=Objects.requireNonNull(b.relationshipId,"Relationship is required");
        issuedAt=Objects.requireNonNull(b.issuedAt,"Issue time is required");
        if(b.lifetimeMinutes<1 || b.lifetimeMinutes>1440) throw new IllegalArgumentException("Lifetime must be between 1 and 1440 minutes");
        lifetimeMinutes=b.lifetimeMinutes;
    }
    public QrCode issue(String secureToken) {
        if(secureToken==null || secureToken.isBlank()) throw new IllegalArgumentException("Token is required");
        QrCode q=new QrCode();q.guardianStudentId=relationshipId;q.createdAt=issuedAt;
        q.expiresAt=issuedAt.plusMinutes(lifetimeMinutes);q.token=secureToken;q.status="ACTIVE";return q;
    }
    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private UUID relationshipId;
        private LocalDateTime issuedAt;
        private int lifetimeMinutes=30;
        public Builder forRelationship(UUID id) { relationshipId=id;return this; }
        public Builder issuedAt(LocalDateTime time) { issuedAt=time;return this; }
        public Builder validForMinutes(int minutes) { lifetimeMinutes=minutes;return this; }
        public QrIssuePlan build() { return new QrIssuePlan(this); }
    }
}
