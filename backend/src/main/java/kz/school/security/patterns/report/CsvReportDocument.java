package kz.school.security.patterns.report;
import java.util.*;
import java.nio.charset.StandardCharsets;
public class CsvReportDocument implements ReportDocument {
    public String contentType(){return "text/csv;charset=UTF-8";}
    public String extension(){return "csv";}
    public byte[] render(List<ReportEntry> entries){
        StringBuilder out=new StringBuilder("id,guardian,student,guard,result,reason,time\r\n");
        for(var e:entries)out.append(String.join(",",List.of(e.id(),e.guardian(),e.student(),e.guard(),e instanceof ReportEntry.Allowed?"ALLOWED":"DENIED",e.reason(),e.time().toString()).stream().map(CsvReportDocument::cell).toList())).append("\r\n");
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }
    static String cell(String value){
        // Prevent spreadsheet formula evaluation, including formulas preceded by whitespace.
        String trimmed=value.stripLeading();
        if(!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0))>=0)value="'"+value;
        return "\""+value.replace("\"","\"\"")+"\"";
    }
}
