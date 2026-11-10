package kz.school.security.patterns.report;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
public class JsonReportDocument implements ReportDocument {
    private final ObjectMapper json;
    public JsonReportDocument(ObjectMapper json){this.json=json;}
    public String contentType(){return "application/json";}
    public String extension(){return "json";}
    public byte[] render(List<ReportEntry> entries){
        var rows=entries.stream().map(e->Map.of("id",e.id(),"guardian",e.guardian(),"student",e.student(),"guard",e.guard(),
            "result",e instanceof ReportEntry.Allowed?"ALLOWED":"DENIED","reason",e.reason(),"time",e.time().toString())).toList();
        try{return json.writerWithDefaultPrettyPrinter().writeValueAsBytes(rows);}
        catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("Cannot render report",e);}
    }
}
