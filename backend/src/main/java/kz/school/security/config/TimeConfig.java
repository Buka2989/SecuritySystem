package kz.school.security.config;
import java.time.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class TimeConfig {
    @Bean Clock clock(@Value("${app.zone}") String zone) { return Clock.system(ZoneId.of(zone)); }
}
