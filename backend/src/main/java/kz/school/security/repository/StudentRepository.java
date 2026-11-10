package kz.school.security.repository;
import kz.school.security.entity.Student;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface StudentRepository extends JpaRepository<Student, UUID> {
    @Query(value="select * from students where full_name ilike concat('%',:name,'%') and class_name ilike concat('%',:className,'%') and status='ACTIVE'", nativeQuery=true)
    List<Student> search(String name, String className);
}
