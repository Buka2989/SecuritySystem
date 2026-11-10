package kz.school.security.service;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kz.school.security.entity.*;
import kz.school.security.repository.*;
import kz.school.security.exception.ApiException;
import kz.school.security.dto.DtoMapper;
import static kz.school.security.dto.ApiDtos.*;
@Service @Transactional
public class GuardianService {
    private final GuardianStudentRepository links;
    private final UserRepository users;
    private final StudentRepository students;
    private final VerificationRequestRepository requests;
    private final QrCodeRepository codes;
    private final Clock clock;
    public GuardianService(GuardianStudentRepository links, UserRepository users, StudentRepository students,
                           VerificationRequestRepository requests, QrCodeRepository codes, Clock clock) {
        this.links=links; this.users=users; this.students=students; this.requests=requests; this.codes=codes; this.clock=clock;
    }
    public List<LinkResponse> list(User actor) { return links.findByGuardianIdAndIsActiveTrue(actor.id).stream().map(DtoMapper::link).toList(); }
    public LinkResponse create(User actor, LinkCreate data) {
        users.lockById(actor.id).orElseThrow(() -> ApiException.missing("User not found"));
        if (!students.existsById(data.studentId())) throw ApiException.missing("Student not found");
        var existing=links.idForGuardianAndStudent(actor.id,data.studentId());
        GuardianStudent link;
        if (existing.isPresent()) {
            link=links.lockById(existing.get()).orElseThrow();
            if (link.isActive) throw new ApiException(400,"Guardian is already linked to this student");
        } else {
            link=new GuardianStudent(); link.guardianId=actor.id; link.studentId=data.studentId(); link.createdAt=LocalDateTime.now(clock);
        }
        link.isActive=true; link.relationshipType=data.relationshipType(); link.verificationStatus="PENDING";
        links.saveAndFlush(link);
        if (requests.findByGuardianStudentIdAndStatus(link.id,"PENDING").isEmpty()) newRequest(link);
        return DtoMapper.link(link);
    }
    public void delete(User actor, UUID id) {
        GuardianStudent link=ownedLocked(actor,id);
        if (!link.isActive) throw new ApiException(400,"Student is already removed");
        link.isActive=false;
        for (VerificationRequest r: requests.findByGuardianStudentIdAndStatus(id,"PENDING")) { r.status="CANCELLED"; r.reviewedAt=LocalDateTime.now(clock); }
        // All QR operations acquire the link lock first, including scans and revocation.
        for (QrCode q: codes.findByGuardianStudentIdAndStatus(id,"ACTIVE")) q.status="REVOKED";
    }
    public GuardianStudent ownedLocked(User actor, UUID id) {
        GuardianStudent link=links.lockById(id).orElseThrow(() -> ApiException.missing("Guardian-student relationship not found"));
        if (!actor.id.equals(link.guardianId)) throw ApiException.missing("Guardian-student relationship not found");
        return link;
    }
    public VerificationRequest newRequest(GuardianStudent link) {
        VerificationRequest r=new VerificationRequest(); r.guardianStudentId=link.id; r.status="PENDING"; r.createdAt=LocalDateTime.now(clock);
        return requests.save(r);
    }
}
