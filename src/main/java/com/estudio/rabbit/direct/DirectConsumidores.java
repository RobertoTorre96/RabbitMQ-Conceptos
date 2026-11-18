package com.estudio.rabbit.direct;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class DirectConsumidores {

    @RabbitListener(queues = DirectConfig.QUEUE_INFO)
    public void monitorInfo(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        System.out.println("  [Monitor INFO] " + mensaje);
        try { channel.basicAck(tag, false); } catch (Exception e) { e.printStackTrace(); }
    }

    @RabbitListener(queues = DirectConfig.QUEUE_ERROR)
    public void alertaError(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        System.out.println("  [Alerta ERROR] " + mensaje);
        try { channel.basicAck(tag, false); } catch (Exception e) { e.printStackTrace(); }
    }
}
