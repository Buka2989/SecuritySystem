package kz.school.security.patterns.report;
import java.util.List;

/** Decorator adds privacy filtering without modifying the original report source. */
public class RedactedReportSource implements ReportSource {
    private final ReportSource wrapped;
    public RedactedReportSource(ReportSource wrapped){this.wrapped=wrapped;}
    public List<ReportEntry> entries(){return wrapped.entries().stream().map(e-> e instanceof ReportEntry.Allowed
        ?(ReportEntry)new ReportEntry.Allowed("", "Hidden", "Hidden", "Hidden",e.reason(),e.time())
        :new ReportEntry.Denied("", "Hidden", "Hidden", "Hidden",e.reason(),e.time())).toList();}
}
