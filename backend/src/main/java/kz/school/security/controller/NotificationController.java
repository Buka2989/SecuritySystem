package kz.school.security.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import kz.school.security.entity.User;
import kz.school.security.patterns.events.ReviewInbox;
@RestController @RequestMapping("/notifications") @PreAuthorize("hasAnyRole('PARENT','TRUSTED_PERSON')")
public class NotificationController {
    private final ReviewInbox inbox;
    public NotificationController(ReviewInbox inbox){this.inbox=inbox;}
    @GetMapping public List<ReviewInbox.Notice> list(@AuthenticationPrincipal User user){return inbox.forGuardian(user.id);}
}
