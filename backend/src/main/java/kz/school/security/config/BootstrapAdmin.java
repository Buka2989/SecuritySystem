package kz.school.security.config;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import kz.school.security.service.UserService;
@Configuration
public class BootstrapAdmin {
    @Bean ApplicationRunner bootstrap(UserService users, @Value("${app.bootstrap-admin.username}") String username,
                                      @Value("${app.bootstrap-admin.password}") String password) {
        return args -> users.bootstrap(username,password);
    }
}
