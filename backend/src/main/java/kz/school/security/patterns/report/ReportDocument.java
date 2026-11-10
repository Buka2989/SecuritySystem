package kz.school.security.patterns.report;
import java.util.List;
public interface ReportDocument {
    byte[] render(List<ReportEntry> entries);
    String contentType();String extension();
}
