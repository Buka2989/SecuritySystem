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
@RestController
public class HealthController {
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    public HealthController(org.springframework.jdbc.core.JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @GetMapping("/") public Map<String,String> root() { return Map.of("message","Security System API is running"); }
    @GetMapping("/health") public org.springframework.http.ResponseEntity<?> health() {
        try { jdbc.queryForObject("select 1",Integer.class); return org.springframework.http.ResponseEntity.ok(Map.of("status","ok","database","connected")); }
        catch (org.springframework.dao.DataAccessException e) { return org.springframework.http.ResponseEntity.status(503).body(Map.of("status","error","database","unavailable")); }
    }
}
