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
@RestController @RequestMapping("/auth")
public class AuthController {
    private final UserService service;
    public AuthController(UserService service) { this.service=service; }
    @PostMapping("/login") public Token login(@Valid @RequestBody Login data) { return service.login(data); }
    @GetMapping("/me") public UserResponse me(@AuthenticationPrincipal User user) { return DtoMapper.user(user); }
}
