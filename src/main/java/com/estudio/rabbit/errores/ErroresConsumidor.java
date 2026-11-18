package com.estudio.rabbit.errores;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ErroresConsumidor {

    @RabbitListener(queues = ErroresConfig.QUEUE_TRABAJO, ackMode = "AUTO")
    public void procesar(String texto) {
        if (texto.startsWith("invalido")) {
            // Rechazo por decisión tuya. Ojo: con el retry global del yaml igual pasa por los reintentos
            // (el RetryTemplate de Boot no distingue tipos). Ver nota del cap. 15.
            throw new AmqpRejectAndDontRequeueException("Dato inválido: no tiene arreglo");
        }
        if (texto.startsWith("caido")) {
            throw new IllegalStateException("BD caída"); // transitorio: que reintente
        }
        System.out.println("  [Errores] OK: " + texto);
    }
}
