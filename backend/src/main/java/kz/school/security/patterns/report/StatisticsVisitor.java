package kz.school.security.patterns.report;
import java.util.*;
public class StatisticsVisitor implements ReportVisitor {
    private long allowed,denied;
    private final Map<String,Long> denialReasons=new TreeMap<>();
    public void visit(ReportEntry.Allowed entry){allowed++;}
    public void visit(ReportEntry.Denied entry){denied++;denialReasons.merge(entry.reason(),1L,Long::sum);}
    public record Summary(long total,long allowed,long denied,Map<String,Long> denialReasons){}
    public Summary summary(){return new Summary(allowed+denied,allowed,denied,Map.copyOf(denialReasons));}
}
