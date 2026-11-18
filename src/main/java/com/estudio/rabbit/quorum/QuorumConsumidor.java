package com.estudio.rabbit.quorum;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class QuorumConsumidor {

    @RabbitListener(queues = QuorumConfig.QUEUE_QUORUM) // modo manual del yaml
    public void procesar(String mensaje, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                         @Header(name = "x-delivery-count", defaultValue = "0") long devoluciones)
            throws IOException {
        System.out.println("  [Quorum] entrega nº " + (devoluciones + 1) + ": " + mensaje);
        if (mensaje.toLowerCase().contains("error")) {
            channel.basicNack(tag, false, true); // requeue = true... pero SIN bucle infinito (x-delivery-limit)
        } else {
            channel.basicAck(tag, false);
        }
    }
}
