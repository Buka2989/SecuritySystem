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
@RestController @RequestMapping("/guardian-students") @PreAuthorize("hasAnyRole('PARENT','TRUSTED_PERSON')")
public class GuardianController {
    private final GuardianService service;
    public GuardianController(GuardianService service) { this.service=service; }
    @GetMapping({"","/"}) public List<LinkResponse> list(@AuthenticationPrincipal User user) { return service.list(user); }
    @PostMapping({"","/"}) @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody LinkCreate data) { return service.create(user,data); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable UUID id) { service.delete(user,id); }
}
