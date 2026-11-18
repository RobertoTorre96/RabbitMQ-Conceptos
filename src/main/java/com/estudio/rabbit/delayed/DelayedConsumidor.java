package com.estudio.rabbit.delayed;

import java.time.LocalTime;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DelayedConsumidor {

    // SOLO escucha la cola de destino: la sala de espera NO debe tener listener
    @RabbitListener(queues = DelayedConfig.QUEUE_DESTINO, ackMode = "AUTO")
    public void procesarMensajeFuturo(String texto) {
        System.out.println("  [Consumidor] Mensaje recibido a las " + LocalTime.now() + " -> " + texto);
    }
}
