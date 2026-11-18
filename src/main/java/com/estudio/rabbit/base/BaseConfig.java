package com.estudio.rabbit.base;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** B1: sin exchange declarado, se usa el exchange por defecto ("") con el nombre de la cola como routing key. */
@Configuration
public class BaseConfig {
    public static final String QUEUE_NAME = "queue_spring_basico";

    @Bean
    public Queue baseQueue() {
        return new Queue(QUEUE_NAME, true); // true = durable
    }
}
