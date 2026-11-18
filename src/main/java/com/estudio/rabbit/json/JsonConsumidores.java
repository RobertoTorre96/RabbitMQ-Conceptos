package com.estudio.rabbit.json;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Recibe los pedidos de /json y también los eventos que publica el relay del outbox (cap. 16 del Vol. 2). */
@Component
public class JsonConsumidores {

    @RabbitListener(queues = JsonConfig.QUEUE_JSON, ackMode = "AUTO")
    public void procesarPedido(Pedido pedido) { // el parámetro YA llega como objeto
        System.out.println("  [Almacén] Nuevo pedido recibido: " + pedido);
    }
}
