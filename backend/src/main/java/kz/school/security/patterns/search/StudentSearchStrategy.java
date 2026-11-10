package kz.school.security.patterns.search;
import java.util.List;
import kz.school.security.entity.Student;
public interface StudentSearchStrategy {
    String key();
    List<Student> search(String name, String className);
}
