package kz.school.security.repository;
import kz.school.security.entity.GuardianStudent;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface GuardianStudentRepository extends JpaRepository<GuardianStudent, UUID> {
    List<GuardianStudent> findByGuardianIdAndIsActiveTrue(UUID guardianId);
    Optional<GuardianStudent> findByGuardianIdAndStudentId(UUID guardianId, UUID studentId);
    @Query("select g.id from GuardianStudent g where g.guardianId=:guardianId and g.studentId=:studentId")
    Optional<UUID> idForGuardianAndStudent(UUID guardianId, UUID studentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select g from GuardianStudent g where g.id = :id") Optional<GuardianStudent> lockById(UUID id);
}
