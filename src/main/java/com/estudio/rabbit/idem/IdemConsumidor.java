package com.estudio.rabbit.idem;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class IdemConsumidor {

    private final CobroService cobroService;
    private final AtomicInteger cobros = new AtomicInteger();
    private final AtomicInteger duplicados = new AtomicInteger();

    public IdemConsumidor(CobroService cobroService) { this.cobroService = cobroService; }

    @RabbitListener(queues = IdemConfig.QUEUE, ackMode = "AUTO") // AUTO: el ACK va después del commit
    public void cobrar(String texto, @Header(AmqpHeaders.MESSAGE_ID) String messageId) {
        if (cobroService.procesar(messageId, texto)) {
            cobros.incrementAndGet();
            System.out.println("  [Idem] COBRANDO (una sola vez): " + texto + " [" + messageId + "]");
        } else {
            duplicados.incrementAndGet();
            System.out.println("  [Idem] Duplicado ignorado: " + messageId);
        }
    }

    public int cobros() { return cobros.get(); }
    public int duplicados() { return duplicados.get(); }
}
