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
@RestController @RequestMapping("/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service=service; }
    @GetMapping({"","/"}) @PreAuthorize("hasRole('ADMIN')") public List<UserResponse> list() { return service.list(); }
    @PostMapping({"","/"}) @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreate data, @AuthenticationPrincipal User user) { return service.create(data,user); }
    @PatchMapping("/me") public UserResponse profile(@AuthenticationPrincipal User user, @Valid @RequestBody ProfileUpdate data) { return service.profile(user,data); }
    @PatchMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN')")
    public UserResponse status(@PathVariable UUID id, @Valid @RequestBody StatusUpdate data) { return service.status(id,data); }
}
