package com.estudio.rabbit.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OutboxController {

    private final PedidoService pedidoService;
    private final JdbcTemplate jdbc;

    public OutboxController(PedidoService pedidoService, JdbcTemplate jdbc) {
        this.pedidoService = pedidoService;
        this.jdbc = jdbc;
    }

    @GetMapping("/outbox/pedido")
    public String crear(@RequestParam String producto, @RequestParam double precio) {
        pedidoService.crearPedido(producto, precio);
        return "OK: pedido guardado + evento en la tabla outbox";
    }

    /** Para ver cuántos eventos esperan y cuántos ya salieron. */
    @GetMapping("/outbox/estado")
    public String estado() {
        Integer pendientes = jdbc.queryForObject("SELECT COUNT(*) FROM outbox WHERE enviado = FALSE", Integer.class);
        Integer enviados = jdbc.queryForObject("SELECT COUNT(*) FROM outbox WHERE enviado = TRUE", Integer.class);
        return "pendientes=" + pendientes + " | enviados=" + enviados;
    }
}
