package com.estudio.rabbit.idem;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Puerta de deduplicación (cap. 13): registro del id + efecto en la MISMA transacción (todo o nada). */
@Service
public class CobroService {

    private final JdbcTemplate jdbc;

    public CobroService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** @return true si se cobró; false si era un duplicado (no se repite el efecto). */
    @Transactional
    public boolean procesar(String messageId, String texto) {
        int filas = jdbc.update(
                "INSERT INTO mensajes_procesados(message_id) VALUES (?) ON CONFLICT DO NOTHING", messageId);
        if (filas == 0) {
            return false; // ya estaba: duplicado, no repetir el efecto
        }
        jdbc.update("INSERT INTO cobros(message_id, texto) VALUES (?, ?)", messageId, texto); // el efecto de negocio
        return true;
    }
}
