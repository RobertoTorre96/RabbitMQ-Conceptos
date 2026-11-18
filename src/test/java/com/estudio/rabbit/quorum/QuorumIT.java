package com.estudio.rabbit.quorum;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.estudio.rabbit.RabbitIntegrationBase;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

class QuorumIT extends RabbitIntegrationBase {

    @Autowired RabbitTemplate rabbitTemplate;

    @Test
    void mensajeVenenosoTerminaEnLaDlqSinBucleInfinito() {
        rabbitTemplate.convertAndSend("", QuorumConfig.QUEUE_QUORUM, "error de prueba");

        await().atMost(20, SECONDS).untilAsserted(() -> {
            Long enDlq = rabbitTemplate.execute(ch -> ch.messageCount(QuorumConfig.QUEUE_QUORUM_DLQ));
            assertEquals(1L, enDlq);
        });
    }
}
