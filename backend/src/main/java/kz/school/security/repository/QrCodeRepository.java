package kz.school.security.repository;
import kz.school.security.entity.QrCode;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
    @Query("select q from QrCode q, GuardianStudent g where q.guardianStudentId=g.id and g.guardianId=:guardianId order by q.createdAt desc") List<QrCode> findForGuardian(UUID guardianId);
    Optional<QrCode> findByToken(String token);
    @Query("select q.guardianStudentId from QrCode q where q.token=:token") Optional<UUID> linkIdForToken(String token);
    @Query("select q.id from QrCode q where q.token=:token") Optional<UUID> idForToken(String token);
    @Query("select q.guardianStudentId from QrCode q where q.id=:id") Optional<UUID> linkIdForId(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select q from QrCode q where q.id=:id") Optional<QrCode> lockById(UUID id);
    List<QrCode> findByGuardianStudentIdAndStatus(UUID guardianStudentId, String status);
}
