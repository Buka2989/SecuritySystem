package kz.school.security.controller;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import kz.school.security.patterns.report.*;
@RestController @RequestMapping("/reports/access") @PreAuthorize("hasAnyRole('ADMIN','SECURITY')")
public class ReportController {
    private final AccessReportFacade reports;
    public ReportController(AccessReportFacade reports){this.reports=reports;}
    @GetMapping public StatisticsVisitor.Summary summary(@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to){return reports.summary(from,to);}
    @GetMapping("/export") public ResponseEntity<byte[]> export(@RequestParam(defaultValue="csv") String format,
        @RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(defaultValue="false") boolean redact){
        var result=reports.export(format,from,to,redact);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(result.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+result.filename()+"\"").body(result.bytes());
    }
}
