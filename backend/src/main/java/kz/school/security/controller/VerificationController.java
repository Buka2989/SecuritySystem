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
@RestController @RequestMapping("/verification-requests")
public class VerificationController {
    private final VerificationService service;
    public VerificationController(VerificationService service) { this.service=service; }
    @GetMapping({"","/"}) @PreAuthorize("hasRole('ADMIN')") public List<AdminVerificationResponse> list() { return service.list(); }
    @PostMapping({"","/"}) @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PARENT','TRUSTED_PERSON')")
    public VerificationResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody VerificationCreate data) { return service.create(user,data); }
    @PatchMapping("/{id}/approve") @PreAuthorize("hasRole('ADMIN')")
    public VerificationResponse approve(@PathVariable UUID id) { return service.review(id,true,null); }
    @PatchMapping("/{id}/reject") @PreAuthorize("hasRole('ADMIN')")
    public VerificationResponse reject(@PathVariable UUID id, @Valid @RequestBody Reject data) { return service.review(id,false,data.comment()); }
}
