package kz.school.security.controller;

import kz.school.security.entity.User;
import kz.school.security.entity.Notification;
import kz.school.security.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import static kz.school.security.dto.ApiDtos.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public List<NotificationResponse> mine(@AuthenticationPrincipal User user) {
        return service.listForUser(user.id).stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        service.markAsRead(id, user.id);
        return service.listForUser(user.id).stream()
            .filter(n -> n.id.equals(id)).findFirst().map(this::toResponse).orElseThrow();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.id, n.title, n.message, n.type, n.isRead, n.createdAt);
    }
}
