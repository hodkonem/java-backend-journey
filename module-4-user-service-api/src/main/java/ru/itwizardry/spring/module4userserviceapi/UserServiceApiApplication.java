package ru.itwizardry.spring.module4userserviceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.itwizardry.spring.module4userserviceapi.config.AppKafkaProperties;

@EnableConfigurationProperties(AppKafkaProperties.class)
@SpringBootApplication
public class UserServiceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApiApplication.class, args);
    }
}
