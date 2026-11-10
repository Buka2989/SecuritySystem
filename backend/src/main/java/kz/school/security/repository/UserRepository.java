package kz.school.security.repository;
import kz.school.security.entity.User;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from User u where u.id = :id") Optional<User> lockById(UUID id);
}
