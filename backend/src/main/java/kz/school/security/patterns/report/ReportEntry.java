package kz.school.security.patterns.report;
import java.time.LocalDateTime;
public sealed interface ReportEntry permits ReportEntry.Allowed,ReportEntry.Denied {
    String id(); String guardian();String student();String guard();String reason();LocalDateTime time();
    void accept(ReportVisitor visitor);
    record Allowed(String id,String guardian,String student,String guard,String reason,LocalDateTime time) implements ReportEntry {
        public void accept(ReportVisitor visitor){visitor.visit(this);}
    }
    record Denied(String id,String guardian,String student,String guard,String reason,LocalDateTime time) implements ReportEntry {
        public void accept(ReportVisitor visitor){visitor.visit(this);}
    }
}
