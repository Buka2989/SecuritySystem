package kz.school.security.patterns.report;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kz.school.security.exception.ApiException;

/** Coordinates data adaptation, filtering, decoration, visiting, and format creation. */
@Service @Transactional(readOnly=true)
public class AccessReportFacade {
    private final AccessLogReportAdapter source;
    private final List<ReportExporter> exporters;
    public AccessReportFacade(AccessLogReportAdapter source,List<ReportExporter> exporters){this.source=source;this.exporters=List.copyOf(exporters);}
    private List<ReportEntry> entries(LocalDate from,LocalDate to,boolean redact){
        if(from!=null && to!=null && from.isAfter(to))throw new ApiException(422,"Start date must not be after end date");
        ReportSource selected=redact?new RedactedReportSource(source):source;
        return selected.entries().stream().filter(e->from==null || !e.time().toLocalDate().isBefore(from))
            .filter(e->to==null || !e.time().toLocalDate().isAfter(to)).toList();
    }
    public StatisticsVisitor.Summary summary(LocalDate from,LocalDate to){
        StatisticsVisitor visitor=new StatisticsVisitor();entries(from,to,false).forEach(e->e.accept(visitor));return visitor.summary();
    }
    public ReportExporter.Export export(String format,LocalDate from,LocalDate to,boolean redact){
        ReportExporter exporter=exporters.stream().filter(e->e.format().equals(format)).findFirst().orElseThrow(()->new ApiException(422,"Unsupported report format"));
        return exporter.export(entries(from,to,redact));
    }
}
