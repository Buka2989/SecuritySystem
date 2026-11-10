package kz.school.security.patterns.events;
import java.util.*;
import org.springframework.stereotype.Component;

/** Bounded transient delivery buffer. The underlying review remains persisted in PostgreSQL. */
@Component
public class ReviewInbox implements ReviewSubscriber {
    public record Notice(UUID id,long sequence,UUID relationshipId,String status,String comment) {}
    private record Delivery(UUID guardianId,Notice notice) {}
    private final Deque<Delivery> recent=new ArrayDeque<>();
    private long sequence;
    public synchronized void onReview(ReviewEvent event) {
        recent.addLast(new Delivery(event.guardianId(),new Notice(UUID.randomUUID(),++sequence,event.relationshipId(),event.status(),event.comment())));
        while(recent.size()>1000) recent.removeFirst();
    }
    public synchronized List<Notice> forGuardian(UUID guardianId) {
        return recent.stream().filter(d->d.guardianId().equals(guardianId)).map(Delivery::notice).toList();
    }
}
