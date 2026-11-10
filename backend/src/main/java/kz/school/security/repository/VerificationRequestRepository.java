package kz.school.security.repository;
import kz.school.security.entity.VerificationRequest;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {
    @Query("select v.guardianStudentId from VerificationRequest v where v.id=:id") Optional<UUID> linkIdForId(UUID id);
    List<VerificationRequest> findAllByOrderByCreatedAtDesc();
    List<VerificationRequest> findByGuardianStudentIdAndStatus(UUID guardianStudentId, String status);
}
