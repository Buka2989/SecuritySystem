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
@RestController @RequestMapping("/qr-codes")
public class QrController {
    private final QrService service;
    public QrController(QrService service) { this.service=service; }
    @GetMapping({"","/"}) @PreAuthorize("hasAnyRole('PARENT','TRUSTED_PERSON')") public List<QrResponse> list(@AuthenticationPrincipal User user) { return service.list(user); }
    @PostMapping({"","/"}) @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PARENT','TRUSTED_PERSON')")
    public QrResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody QrCreate data) { return service.create(user,data); }
    @PatchMapping("/{id}/revoke") @PreAuthorize("hasAnyRole('PARENT','TRUSTED_PERSON')")
    public QrResponse revoke(@AuthenticationPrincipal User user, @PathVariable UUID id) { return service.revoke(user,id); }
    @PostMapping("/verify") @PreAuthorize("hasRole('SECURITY')")
    public VerifyResponse verify(@AuthenticationPrincipal User user, @Valid @RequestBody QrVerify data) { return service.verify(user,data); }
}
