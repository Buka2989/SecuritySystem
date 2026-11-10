package kz.school.security.patterns.report;
import java.util.List;

/** Factory Method: subclasses choose the document product used by the export workflow. */
public abstract class ReportExporter {
    public record Export(byte[] bytes,String contentType,String filename){}
    protected abstract ReportDocument createDocument();
    public abstract String format();
    public final Export export(List<ReportEntry> entries){
        ReportDocument document=createDocument();
        return new Export(document.render(List.copyOf(entries)),document.contentType(),"access-report."+document.extension());
    }
}
