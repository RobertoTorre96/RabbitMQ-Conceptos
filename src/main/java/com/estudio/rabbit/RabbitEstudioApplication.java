package com.estudio.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // cap. 16: el relay del outbox usa @Scheduled
public class RabbitEstudioApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitEstudioApplication.class, args);
    }
}
