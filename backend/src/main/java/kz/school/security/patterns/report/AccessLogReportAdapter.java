package kz.school.security.patterns.report;
import java.util.*;
import org.springframework.stereotype.Component;
import kz.school.security.repository.*;

/** Adapts the existing relational audit model to the report's typed entry interface. */
@Component
public class AccessLogReportAdapter implements ReportSource {
    private final AccessLogRepository logs;private final QrCodeRepository codes;
    private final GuardianStudentRepository links;private final UserRepository users;private final StudentRepository students;
    public AccessLogReportAdapter(AccessLogRepository logs,QrCodeRepository codes,GuardianStudentRepository links,UserRepository users,StudentRepository students){
        this.logs=logs;this.codes=codes;this.links=links;this.users=users;this.students=students;
    }
    public List<ReportEntry> entries(){
        return logs.findAllByOrderByCreatedAtDesc().stream().map(log->{
            var q=codes.findById(log.qrCodeId).orElseThrow();var link=links.findById(q.guardianStudentId).orElseThrow();
            String guardian=users.findById(link.guardianId).orElseThrow().fullName;
            String student=students.findById(link.studentId).orElseThrow().fullName;
            String guard=users.findById(log.securityUserId).orElseThrow().fullName;
            return "ALLOWED".equals(log.result)?(ReportEntry)new ReportEntry.Allowed(log.id.toString(),guardian,student,guard,log.reason,log.createdAt)
                :new ReportEntry.Denied(log.id.toString(),guardian,student,guard,log.reason,log.createdAt);
        }).toList();
    }
}
