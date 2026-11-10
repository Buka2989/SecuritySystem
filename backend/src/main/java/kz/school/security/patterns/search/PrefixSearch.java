package kz.school.security.patterns.search;
import java.util.List;
import org.springframework.stereotype.Component;
import kz.school.security.entity.Student;
import kz.school.security.repository.StudentRepository;
@Component
public class PrefixSearch implements StudentSearchStrategy {
    private final StudentRepository students;
    public PrefixSearch(StudentRepository students) { this.students=students; }
    public String key() { return "prefix"; }
    public List<Student> search(String name,String className) { return students.searchPrefix(name,className); }
}
