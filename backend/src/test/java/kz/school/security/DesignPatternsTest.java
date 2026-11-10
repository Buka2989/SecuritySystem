package kz.school.security;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.support.*;
import kz.school.security.patterns.qr.*;
import kz.school.security.patterns.events.*;
import kz.school.security.patterns.report.*;

class DesignPatternsTest {
    final LocalDateTime time=LocalDateTime.of(2026,11,1,10,0);
    final ReportEntry.Allowed allowed=new ReportEntry.Allowed("a","Guardian","Child","Guard","VALID_QR",time);
    final ReportEntry.Denied denied=new ReportEntry.Denied("b","Guardian","Child","Guard","QR_ALREADY_USED",time);
    @Test void builderAppliesLifetimeAndDefaults(){
        var id=UUID.randomUUID();var q=QrIssuePlan.builder().forRelationship(id).issuedAt(time).validForMinutes(5).build().issue("secure-token");
        assertThat(q.expiresAt).isEqualTo(time.plusMinutes(5));assertThat(q.guardianStudentId).isEqualTo(id);
        assertThat(QrIssuePlan.builder().forRelationship(id).issuedAt(time).build().issue("token").expiresAt).isEqualTo(time.plusMinutes(30));
    }
    @Test void builderRejectsInvalidConfiguration(){
        for(int minutes:new int[]{0,1441})assertThatThrownBy(()->QrIssuePlan.builder().forRelationship(UUID.randomUUID()).issuedAt(time).validForMinutes(minutes).build()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->QrIssuePlan.builder().issuedAt(time).build()).isInstanceOf(NullPointerException.class);
    }
    @Test void decoratorRedactsWithoutMutatingSource(){
        ReportSource original=()->List.of(allowed,denied);var result=new RedactedReportSource(original).entries();
        assertThat(result).hasSize(2);assertThat(result.getFirst().guardian()).isEqualTo("Hidden");assertThat(result.getFirst().id()).isEmpty();
        assertThat(original.entries().getFirst().guardian()).isEqualTo("Guardian");assertThat(result.get(1).reason()).isEqualTo("QR_ALREADY_USED");
    }
    @Test void visitorCountsOutcomesAndReasons(){
        var visitor=new StatisticsVisitor();allowed.accept(visitor);denied.accept(visitor);denied.accept(visitor);
        assertThat(visitor.summary().total()).isEqualTo(3);assertThat(visitor.summary().allowed()).isEqualTo(1);
        assertThat(visitor.summary().denialReasons()).containsEntry("QR_ALREADY_USED",2L);
    }
    @Test void factoryMethodsProduceDifferentUsableDocuments() throws Exception {
        var csv=new CsvReportExporter().export(List.of(allowed));var json=new JsonReportExporter(new ObjectMapper()).export(List.of(allowed));
        assertThat(csv.filename()).endsWith(".csv");assertThat(new String(csv.bytes(),java.nio.charset.StandardCharsets.UTF_8)).contains("ALLOWED");
        assertThat(new ObjectMapper().readTree(json.bytes()).get(0).get("student").asText()).isEqualTo("Child");
    }
    @Test void csvEscapesQuotesAndNeutralizesFormulas(){
        var row=new ReportEntry.Allowed("id"," =SUM(1,2)","A\"B","guard","VALID_QR",time);
        String csv=new String(new CsvReportDocument().render(List.of(row)),java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("\"' =SUM(1,2)\"").contains("\"A\"\"B\"");
    }
    @Test void observerDeliversOnlyAfterCommitAndOnlyToOwner(){
        var inbox=new ReviewInbox();var publisher=new ReviewPublisher(List.of(inbox));UUID guardian=UUID.randomUUID();
        TransactionSynchronizationManager.initSynchronization();
        try{
            publisher.publishAfterCommit(new ReviewEvent(guardian,UUID.randomUUID(),"APPROVED",null));
            assertThat(inbox.forGuardian(guardian)).isEmpty();
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            assertThat(inbox.forGuardian(guardian)).hasSize(1);assertThat(inbox.forGuardian(UUID.randomUUID())).isEmpty();
        }finally{TransactionSynchronizationManager.clearSynchronization();}
    }
    @Test void failingObserverDoesNotBlockOtherSubscribers(){
        var inbox=new ReviewInbox();ReviewSubscriber broken=e->{throw new IllegalStateException();};var publisher=new ReviewPublisher(List.of(broken,inbox));
        UUID id=UUID.randomUUID();publisher.dispatch(new ReviewEvent(id,UUID.randomUUID(),"REJECTED","Reason"));assertThat(inbox.forGuardian(id)).hasSize(1);
        publisher.unsubscribe(inbox);publisher.dispatch(new ReviewEvent(id,UUID.randomUUID(),"APPROVED",null));assertThat(inbox.forGuardian(id)).hasSize(1);
    }
    @Test void rolledBackReviewDoesNotNotifyGuardian(){
        var inbox=new ReviewInbox();var publisher=new ReviewPublisher(List.of(inbox));UUID guardian=UUID.randomUUID();
        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publishAfterCommit(new ReviewEvent(guardian,UUID.randomUUID(),"APPROVED",null));
            TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            assertThat(inbox.forGuardian(guardian)).isEmpty();
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
        assertThatThrownBy(()->publisher.publishAfterCommit(new ReviewEvent(guardian,UUID.randomUUID(),"APPROVED",null)))
            .isInstanceOf(IllegalStateException.class);
    }
    @Test void facadeFiltersDatesInclusivelyAndRejectsInvalidOptions(){
        ReportSource source=()->List.of(allowed,denied);
        var facade=new AccessReportFacade(source,List.of(new CsvReportExporter()));
        assertThat(facade.summary(time.toLocalDate(),time.toLocalDate()).total()).isEqualTo(2);
        assertThat(facade.summary(time.toLocalDate().plusDays(1),null).total()).isZero();
        assertThatThrownBy(()->facade.summary(time.toLocalDate().plusDays(1),time.toLocalDate()))
            .isInstanceOf(kz.school.security.exception.ApiException.class).hasMessageContaining("Start date");
        assertThatThrownBy(()->facade.export("xml",null,null,false))
            .isInstanceOf(kz.school.security.exception.ApiException.class).hasMessageContaining("Unsupported");
    }
}
