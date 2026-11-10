package kz.school.security.patterns.report;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class JsonReportExporter extends ReportExporter {
    private final ObjectMapper json;
    public JsonReportExporter(ObjectMapper json){this.json=json;}
    protected ReportDocument createDocument(){return new JsonReportDocument(json);}
    public String format(){return "json";}
}
