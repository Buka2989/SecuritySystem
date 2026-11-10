package kz.school.security.repository;
import kz.school.security.entity.AccessLog;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
    List<AccessLog> findAllByOrderByCreatedAtDesc();
}
