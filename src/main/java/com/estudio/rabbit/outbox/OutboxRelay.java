package com.estudio.rabbit.outbox;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelay {

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbitTemplate;

    public OutboxRelay(JdbcTemplate jdbc, RabbitTemplate rabbitTemplate) {
        this.jdbc = jdbc;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 2000) // cada 2 s (requiere @EnableScheduling)
    public void publicarPendientes() {
        var filas = jdbc.queryForList("SELECT id, exchange_name, routing_key, payload FROM outbox "
                + "WHERE enviado = FALSE ORDER BY creado LIMIT 50");
        for (var fila : filas) {
            String id = (String) fila.get("id");
            try {
                Message msg = MessageBuilder
                        .withBody(((String) fila.get("payload")).getBytes(StandardCharsets.UTF_8))
                        .setContentType("application/json")
                        .setMessageId(id) // estable en cada reintento
                        .build();
                CorrelationData cd = new CorrelationData(id);
                rabbitTemplate.send((String) fila.get("exchange_name"), (String) fila.get("routing_key"), msg, cd);

                boolean ack = cd.getFuture().get(5, TimeUnit.SECONDS).isAck(); // espera el confirm
                boolean devuelto = cd.getReturned() != null;                   // sin ruta: igual llega ACK, lo tratamos como fallo
                if (ack && !devuelto) {
                    jdbc.update("UPDATE outbox SET enviado = TRUE WHERE id = ?", id);
                } else {
                    System.err.println("  [Outbox] ack=" + ack + " devuelto=" + devuelto + " " + id + ": queda pendiente");
                }
            } catch (Exception e) {
                System.err.println("  [Outbox] Sin confirmar " + id + ": queda pendiente");
            }
        }
    }
}
