package kz.school.security.patterns.events;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.*;
import org.slf4j.LoggerFactory;

/** Application-owned Observer implementation; Spring only supplies transaction completion. */
@Component
public class ReviewPublisher {
    private final CopyOnWriteArrayList<ReviewSubscriber> subscribers;
    public ReviewPublisher(List<ReviewSubscriber> subscribers) { this.subscribers=new CopyOnWriteArrayList<>(subscribers); }
    public void subscribe(ReviewSubscriber subscriber) { subscribers.addIfAbsent(subscriber); }
    public void unsubscribe(ReviewSubscriber subscriber) { subscribers.remove(subscriber); }
    public void publishAfterCommit(ReviewEvent event) {
        if(!TransactionSynchronizationManager.isSynchronizationActive()) throw new IllegalStateException("Review publication requires a transaction");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { dispatch(event); }
        });
    }
    public void dispatch(ReviewEvent event) {
        for(ReviewSubscriber subscriber:subscribers) {
            try { subscriber.onReview(event); }
            catch(RuntimeException e) { LoggerFactory.getLogger(ReviewPublisher.class).warn("Review subscriber failed: {}",subscriber.getClass().getSimpleName()); }
        }
    }
}
