package com.estudio.rabbit.retry;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RetryConsumidores {

    // Anulamos el modo manual del YAML solo para este método
    @RabbitListener(queues = RetryConfig.QUEUE_RETRY, ackMode = "AUTO")
    public void procesarConReintentos(String mensaje) throws Exception {
        System.out.println("  [Obrero Retry] Procesando: '" + mensaje + "'");
        if (mensaje.toLowerCase().contains("error")) {
            throw new RuntimeException("Caída temporal de la base de datos"); // Spring reintenta
        }
        System.out.println("  [v] Éxito. Spring enviará el ACK automático.");
    }

    @RabbitListener(queues = RetryConfig.QUEUE_RETRY_DLQ, ackMode = "AUTO")
    public void procesarRetryDlq(String mensaje) {
        System.out.println("  [Hospital Retry] Rescatado tras agotar los 3 intentos: '" + mensaje + "'");
    }
}
