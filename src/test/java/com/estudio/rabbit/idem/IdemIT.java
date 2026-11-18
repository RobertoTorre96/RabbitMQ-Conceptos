package com.estudio.rabbit.idem;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.estudio.rabbit.RabbitIntegrationBase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class IdemIT extends RabbitIntegrationBase {

    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired IdemConsumidor consumidor;

    @Test
    void mismoMessageIdSeProcesaUnaSolaVez() {
        String id = "pago-" + UUID.randomUUID();
        int cobros0 = consumidor.cobros();
        int dup0 = consumidor.duplicados();

        for (int i = 0; i < 2; i++) {
            rabbitTemplate.convertAndSend(IdemConfig.EXCHANGE, IdemConfig.ROUTING, "cobro", m -> {
                m.getMessageProperties().setMessageId(id);
                return m;
            });
        }

        await().atMost(10, SECONDS).untilAsserted(() -> { // asíncrono: esperamos, no dormimos
            assertEquals(cobros0 + 1, consumidor.cobros());
            assertEquals(dup0 + 1, consumidor.duplicados());
        });
    }
}
