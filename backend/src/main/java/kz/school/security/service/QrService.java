package kz.school.security.service;
import java.time.*;
import java.util.*;
import java.security.SecureRandom;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kz.school.security.entity.*;
import kz.school.security.repository.*;
import kz.school.security.exception.ApiException;
import kz.school.security.dto.DtoMapper;
import static kz.school.security.dto.ApiDtos.*;
@Service @Transactional
public class QrService {
    private static final Logger LOG=LoggerFactory.getLogger(QrService.class);
    private final QrCodeRepository codes;
    private final GuardianStudentRepository links;
    private final UserRepository users;
    private final StudentRepository students;
    private final AccessLogRepository logs;
    private final GuardianService guardians;
    private final Clock clock;
    private final SecureRandom random=new SecureRandom();
    public QrService(QrCodeRepository codes, GuardianStudentRepository links, UserRepository users, StudentRepository students,
                     AccessLogRepository logs, GuardianService guardians, Clock clock) {
        this.codes=codes; this.links=links; this.users=users; this.students=students; this.logs=logs; this.guardians=guardians; this.clock=clock;
    }
    public List<QrResponse> list(User actor) { return codes.findForGuardian(actor.id).stream().map(DtoMapper::qr).toList(); }
    public QrResponse create(User actor, QrCreate data) {
        GuardianStudent link=guardians.ownedLocked(actor,data.guardianStudentId());
        if (!link.isActive || !"APPROVED".equals(link.verificationStatus)) throw new ApiException(403,"Guardian-student relationship is not approved");
        Student student=students.findById(link.studentId).orElseThrow(() -> ApiException.missing("Student not found"));
        if (!"ACTIVE".equals(student.status)) throw new ApiException(403,"Student is inactive");
        byte[] bytes=new byte[32]; random.nextBytes(bytes);
        QrCode q=new QrCode(); q.guardianStudentId=link.id; q.token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        q.createdAt=LocalDateTime.now(clock); q.expiresAt=q.createdAt.plusMinutes(data.expiresInMinutes()==null?30:data.expiresInMinutes()); q.status="ACTIVE";
        return DtoMapper.qr(codes.save(q));
    }
    public QrResponse revoke(User actor, UUID id) {
        UUID linkId=codes.linkIdForId(id).orElseThrow(() -> ApiException.missing("QR code not found"));
        GuardianStudent link=links.lockById(linkId).orElseThrow(() -> ApiException.missing("QR code not found"));
        if (!actor.id.equals(link.guardianId)) throw ApiException.missing("QR code not found");
        QrCode q=codes.lockById(id).orElseThrow(() -> ApiException.missing("QR code not found"));
        if (!"ACTIVE".equals(q.status)) throw new ApiException(400,"Only active QR codes can be revoked");
        q.status="REVOKED"; return DtoMapper.qr(q);
    }
    public VerifyResponse verify(User guard, QrVerify data) {
        Optional<UUID> linkId=codes.linkIdForToken(data.token());
        if (linkId.isEmpty()) {
            // Existing NOT NULL FK prevents logging unknown tokens to this table. Never fabricate database records.
            LOG.info("QR scan denied: reason=INVALID_QR security_user_id={}",guard.id);
            return VerifyResponse.denied("INVALID_QR");
        }
        GuardianStudent link=links.lockById(linkId.get()).orElseThrow();
        UUID id=codes.idForToken(data.token()).orElseThrow();
        QrCode q=codes.lockById(id).orElseThrow();
        LocalDateTime now=LocalDateTime.now(clock);
        User guardian=users.findById(link.guardianId).orElseThrow(); Student student=students.findById(link.studentId).orElseThrow();
        String reason=QrPolicy.rejection(q,link,guardian,student,now);
        if (reason==null) { q.status="USED"; q.usedAt=now; } else if ("QR_EXPIRED".equals(reason)) q.status="EXPIRED";
        AccessLog log=new AccessLog(); log.qrCodeId=q.id; log.securityUserId=guard.id; log.action="ENTRY";
        log.result=reason==null?"ALLOWED":"DENIED"; log.reason=reason==null?"VALID_QR":reason; log.createdAt=now; logs.save(log);
        return reason==null ? new VerifyResponse("ALLOWED","VALID_QR",guardian.fullName,student.fullName,student.className,now) : VerifyResponse.denied(reason);
    }
    public List<LogResponse> logs() { return logs.findAllByOrderByCreatedAtDesc().stream().map(DtoMapper::log).toList(); }
}
