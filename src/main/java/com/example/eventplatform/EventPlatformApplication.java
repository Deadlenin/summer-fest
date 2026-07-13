package com.example.eventplatform;

import com.example.eventplatform.config.AdminProperties;
import com.example.eventplatform.config.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AdminProperties.class, NotificationProperties.class})
public class EventPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventPlatformApplication.class, args);
    }
}
