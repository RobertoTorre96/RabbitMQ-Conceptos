package com.estudio.rabbit.idem;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdemProductor {

    private final RabbitTemplate rabbitTemplate;
    private final JdbcTemplate jdbc;
    private final IdemConsumidor consumidor;

    public IdemProductor(RabbitTemplate rabbitTemplate, JdbcTemplate jdbc, IdemConsumidor consumidor) {
        this.rabbitTemplate = rabbitTemplate;
        this.jdbc = jdbc;
        this.consumidor = consumidor;
    }

    @GetMapping("/idem")
    public String enviarDuplicado(@RequestParam String id, @RequestParam String texto) {
        for (int i = 1; i <= 2; i++) { // simulamos un duplicado: mismo id, 2 envíos
            rabbitTemplate.convertAndSend(IdemConfig.EXCHANGE, IdemConfig.ROUTING, texto, m -> {
                m.getMessageProperties().setMessageId(id); // el id viaja DENTRO del mensaje
                return m;
            });
        }
        return "Enviado 2 veces con messageId=" + id;
    }

    /** Para mirar el resultado sin abrir DBeaver. */
    @GetMapping("/idem/estado")
    public String estado() {
        Integer filas = jdbc.queryForObject("SELECT COUNT(*) FROM cobros", Integer.class);
        return "cobros en la BD=" + filas + " | cobros=" + consumidor.cobros()
                + " | duplicados ignorados=" + consumidor.duplicados();
    }
}
