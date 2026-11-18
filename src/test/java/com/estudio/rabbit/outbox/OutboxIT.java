package com.estudio.rabbit.outbox;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.estudio.rabbit.RabbitIntegrationBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class OutboxIT extends RabbitIntegrationBase {

    @Autowired PedidoService pedidoService;
    @Autowired JdbcTemplate jdbc;

    @Test
    void elRelayPublicaYMarcaComoEnviado() {
        pedidoService.crearPedido("Laptop-test", 1500);

        await().atMost(15, SECONDS).untilAsserted(() -> {
            Integer pendientes = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM outbox WHERE enviado = FALSE", Integer.class);
            assertEquals(0, pendientes);
        });
    }
}
