package kz.school.security.patterns.report;
import org.springframework.stereotype.Component;
@Component
public class CsvReportExporter extends ReportExporter {
    protected ReportDocument createDocument(){return new CsvReportDocument();}
    public String format(){return "csv";}
}
