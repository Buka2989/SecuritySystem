package kz.school.security.patterns.report;
public interface ReportVisitor {
    void visit(ReportEntry.Allowed entry);
    void visit(ReportEntry.Denied entry);
}
