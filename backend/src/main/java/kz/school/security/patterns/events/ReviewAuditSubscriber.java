package kz.school.security.patterns.events;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
@Component
public class ReviewAuditSubscriber implements ReviewSubscriber {
    public void onReview(ReviewEvent event) {
        LoggerFactory.getLogger(ReviewAuditSubscriber.class).info("Verification committed: relationship={} status={}",event.relationshipId(),event.status());
    }
}
