package kz.school.security;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.time.*;
import kz.school.security.entity.*;
import kz.school.security.service.QrPolicy;
class QrPolicyTest {
    private final LocalDateTime now=LocalDateTime.of(2026,1,1,12,0);
    private QrCode qr() { QrCode q=new QrCode(); q.status="ACTIVE"; q.expiresAt=now.plusMinutes(1); return q; }
    private GuardianStudent link() { GuardianStudent g=new GuardianStudent();g.isActive=true;g.verificationStatus="APPROVED";return g; }
    private User user() { User u=new User();u.status="ACTIVE";return u; }
    private Student student() { Student s=new Student();s.status="ACTIVE";return s; }
    @Test void activeApprovedQrIsAllowed() { assertThat(QrPolicy.rejection(qr(),link(),user(),student(),now)).isNull(); }
    @Test void expiryIsExclusive() { QrCode q=qr();q.expiresAt=now;assertThat(QrPolicy.rejection(q,link(),user(),student(),now)).isEqualTo("QR_EXPIRED"); }
    @Test void usedStatusTakesPrecedenceOverExpiry() { QrCode q=qr();q.status="USED";q.expiresAt=now.minusDays(1);assertThat(QrPolicy.rejection(q,link(),user(),student(),now)).isEqualTo("QR_ALREADY_USED"); }
    @Test void revokedQrIsDenied() { QrCode q=qr();q.status="REVOKED";assertThat(QrPolicy.rejection(q,link(),user(),student(),now)).isEqualTo("QR_REVOKED"); }
    @Test void inactiveLinkIsDenied() { GuardianStudent g=link();g.isActive=false;assertThat(QrPolicy.rejection(qr(),g,user(),student(),now)).isEqualTo("NOT_VERIFIED"); }
    @Test void unapprovedLinkIsDenied() { GuardianStudent g=link();g.verificationStatus="PENDING";assertThat(QrPolicy.rejection(qr(),g,user(),student(),now)).isEqualTo("NOT_VERIFIED"); }
    @Test void blockedGuardianIsDenied() { User u=user();u.status="BLOCKED";assertThat(QrPolicy.rejection(qr(),link(),u,student(),now)).isEqualTo("NOT_VERIFIED"); }
    @Test void inactiveStudentIsDenied() { Student s=student();s.status="INACTIVE";assertThat(QrPolicy.rejection(qr(),link(),user(),s,now)).isEqualTo("NOT_VERIFIED"); }
}
