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
@RestController @RequestMapping("/access-logs") @PreAuthorize("hasAnyRole('ADMIN','SECURITY')")
public class AccessLogController {
    private final QrService service;
    public AccessLogController(QrService service) { this.service=service; }
    @GetMapping({"","/"}) public List<LogResponse> list() { return service.logs(); }
}
