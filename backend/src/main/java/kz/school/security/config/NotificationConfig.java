package kz.school.security.config;

import kz.school.security.factory.InAppNotificationCreator;
import kz.school.security.factory.NotificationCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {
    @Bean
    NotificationCreator notificationCreator() {
        return new InAppNotificationCreator();
    }
}
