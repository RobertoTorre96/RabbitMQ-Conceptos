package com.estudio.rabbit.outbox;

import com.estudio.rabbit.json.JsonConfig;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final JdbcTemplate jdbc;

    public PedidoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional // las dos filas o ninguna
    public void crearPedido(String producto, double precio) {
        int pedidoId = ThreadLocalRandom.current().nextInt(1, 100_000);
        jdbc.update("INSERT INTO pedidos(id, producto, precio) VALUES (?, ?, ?)", pedidoId, producto, precio);

        // el evento que verá el consumidor (en serio: serializá con un mapper JSON)
        String json = String.format(Locale.ROOT,
                "{\"id\":%d,\"producto\":\"%s\",\"precio\":%.1f}", pedidoId, producto, precio);
        jdbc.update("INSERT INTO outbox(id, exchange_name, routing_key, payload) VALUES (?, ?, ?, ?)",
                UUID.randomUUID().toString(), JsonConfig.EXCHANGE_JSON, JsonConfig.ROUTING_JSON, json);
    }
}
