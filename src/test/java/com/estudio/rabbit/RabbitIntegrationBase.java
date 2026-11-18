package com.estudio.rabbit;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

/**
 * Un RabbitMQ y un PostgreSQL reales y descartables para TODAS las clases *IT (contenedores singleton).
 * Necesita Docker corriendo. No usa tu docker-compose: levanta contenedores propios y los borra al terminar.
 */
@SpringBootTest
public abstract class RabbitIntegrationBase {

    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4-management");
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    static {
        RABBIT.start();
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
