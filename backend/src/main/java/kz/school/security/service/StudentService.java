package kz.school.security.service;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kz.school.security.repository.StudentRepository;
import kz.school.security.entity.Student;
import kz.school.security.exception.ApiException;
import kz.school.security.dto.DtoMapper;
import static kz.school.security.dto.ApiDtos.*;
@Service @Transactional
public class StudentService {
    private final StudentRepository students;
    private final Clock clock;
    private final List<kz.school.security.patterns.search.StudentSearchStrategy> strategies;
    public StudentService(StudentRepository students, Clock clock, List<kz.school.security.patterns.search.StudentSearchStrategy> strategies) { this.students=students; this.clock=clock;this.strategies=List.copyOf(strategies); }
    public List<StudentResponse> list() { return students.findAll().stream().map(DtoMapper::student).toList(); }
    public List<StudentResponse> search(String name,String className) { return students.search(name,className).stream().map(DtoMapper::student).toList(); }
    public List<StudentResponse> search(String name,String className,String mode) {
        return strategies.stream().filter(s->s.key().equals(mode)).findFirst().orElseThrow(()->new ApiException(422,"Unknown search mode"))
            .search(name,className).stream().map(DtoMapper::student).toList();
    }
    public StudentResponse update(UUID id,StudentCreate data) {
        Student s=students.findById(id).orElseThrow(()->ApiException.missing("Student not found"));
        s.fullName=data.fullName();s.className=data.className();if(data.status()!=null)s.status=data.status();s.updatedAt=LocalDateTime.now(clock);
        return DtoMapper.student(s);
    }
    public StudentResponse get(UUID id) { return DtoMapper.student(students.findById(id).orElseThrow(() -> ApiException.missing("Student not found"))); }
    public StudentResponse create(StudentCreate data) {
        Student s=new Student(); s.fullName=data.fullName(); s.className=data.className(); s.status=data.status()==null?"ACTIVE":data.status();
        s.createdAt=LocalDateTime.now(clock); s.updatedAt=s.createdAt; return DtoMapper.student(students.save(s));
    }
}
