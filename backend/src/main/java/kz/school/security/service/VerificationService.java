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
public class VerificationService {
    private final VerificationRequestRepository requests;
    private final GuardianStudentRepository links;
    private final UserRepository users;
    private final StudentRepository students;
    private final GuardianService guardians;
    private final Clock clock;
    private final kz.school.security.patterns.events.ReviewPublisher events;
    public VerificationService(VerificationRequestRepository requests, GuardianStudentRepository links, UserRepository users,
                               StudentRepository students, GuardianService guardians, Clock clock, kz.school.security.patterns.events.ReviewPublisher events) {
        this.requests=requests; this.links=links; this.users=users; this.students=students; this.guardians=guardians; this.clock=clock;this.events=events;
    }
    public List<AdminVerificationResponse> list() {
        return requests.findAllByOrderByCreatedAtDesc().stream().map(r -> {
            GuardianStudent g=links.findById(r.guardianStudentId).orElseThrow();
            User u=users.findById(g.guardianId).orElseThrow(); Student s=students.findById(g.studentId).orElseThrow();
            return new AdminVerificationResponse(r.id,g.id,u.fullName,s.fullName,s.className,g.relationshipType,r.status,r.comment,r.createdAt,r.reviewedAt);
        }).toList();
    }
    public VerificationResponse create(User actor, VerificationCreate data) {
        GuardianStudent link=guardians.ownedLocked(actor,data.guardianStudentId());
        if (!link.isActive) throw new ApiException(400,"Student is already removed");
        if ("APPROVED".equals(link.verificationStatus)) throw new ApiException(400,"Relationship is already approved");
        if (!requests.findByGuardianStudentIdAndStatus(link.id,"PENDING").isEmpty()) throw new ApiException(400,"Verification request already exists");
        link.verificationStatus="PENDING"; return DtoMapper.verification(guardians.newRequest(link));
    }
    public VerificationResponse review(UUID id, boolean approve, String comment) {
        UUID linkId=requests.linkIdForId(id).orElseThrow(() -> ApiException.missing("Verification request not found"));
        GuardianStudent link=links.lockById(linkId).orElseThrow(() -> ApiException.missing("Guardian-student relationship not found"));
        // Read after locking to observe the latest committed review/delete.
        VerificationRequest r=requests.findById(id).orElseThrow(() -> ApiException.missing("Verification request not found"));
        if (!"PENDING".equals(r.status)) throw new ApiException(400,"Verification request is already processed");
        if (!link.isActive) throw new ApiException(400,"Student is already removed");
        r.status=approve?"APPROVED":"REJECTED"; r.reviewedAt=LocalDateTime.now(clock); if (!approve) r.comment=comment;
        link.verificationStatus=r.status;
        events.publishAfterCommit(new kz.school.security.patterns.events.ReviewEvent(link.guardianId,link.id,r.status,r.comment));
        return DtoMapper.verification(r);
    }
}
