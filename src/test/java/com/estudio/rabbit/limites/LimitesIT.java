package com.estudio.rabbit.limites;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.estudio.rabbit.RabbitIntegrationBase;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

class LimitesIT extends RabbitIntegrationBase {

    @Autowired RabbitTemplate rabbitTemplate;

    @Test
    void colaLimitadaNoPasaDeCinco() {
        for (int i = 1; i <= 8; i++) {
            rabbitTemplate.convertAndSend("", LimitesConfig.QUEUE_LIMITADA, "m" + i);
        }
        await().atMost(5, SECONDS).untilAsserted(() -> {
            Long cantidad = rabbitTemplate.execute(ch -> ch.messageCount(LimitesConfig.QUEUE_LIMITADA));
            assertEquals(5L, cantidad); // el estado del BROKER, no solo el de tu código
        });
    }
}
