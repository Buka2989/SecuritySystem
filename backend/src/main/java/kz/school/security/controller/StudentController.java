package kz.school.security.controller;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import kz.school.security.entity.User;
import kz.school.security.service.*;
import kz.school.security.dto.DtoMapper;
import static kz.school.security.dto.ApiDtos.*;
@RestController @RequestMapping("/students")
public class StudentController {
    private final StudentService service;
    public StudentController(StudentService service) { this.service=service; }
    @GetMapping({"","/"}) public List<StudentResponse> list() { return service.list(); }
    @GetMapping("/search") public List<StudentResponse> search(@RequestParam("full_name") @Size(min=2) String name,
        @RequestParam("class_name") @Size(min=1) String className) { return service.search(name,className); }
    @GetMapping("/{id}") public StudentResponse get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping({"","/"}) @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    public StudentResponse create(@Valid @RequestBody StudentCreate data) { return service.create(data); }
}
