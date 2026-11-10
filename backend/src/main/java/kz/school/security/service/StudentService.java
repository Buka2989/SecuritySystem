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
    public StudentService(StudentRepository students, Clock clock) { this.students=students; this.clock=clock; }
    public List<StudentResponse> list() { return students.findAll().stream().map(DtoMapper::student).toList(); }
    public List<StudentResponse> search(String name,String className) { return students.search(name,className).stream().map(DtoMapper::student).toList(); }
    public StudentResponse get(UUID id) { return DtoMapper.student(students.findById(id).orElseThrow(() -> ApiException.missing("Student not found"))); }
    public StudentResponse create(StudentCreate data) {
        Student s=new Student(); s.fullName=data.fullName(); s.className=data.className(); s.status=data.status()==null?"ACTIVE":data.status();
        s.createdAt=LocalDateTime.now(clock); s.updatedAt=s.createdAt; return DtoMapper.student(students.save(s));
    }
}
