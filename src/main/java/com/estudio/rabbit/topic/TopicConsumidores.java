package com.estudio.rabbit.topic;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class TopicConsumidores {

    @RabbitListener(queues = TopicConfig.QUEUE_USA)
    public void noticieroUsa(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        System.out.println("  [Noticiero USA] " + mensaje);
        ack(channel, tag);
    }

    @RabbitListener(queues = TopicConfig.QUEUE_DEPORTES)
    public void canalDeportes(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        System.out.println("  [Canal Deportes] " + mensaje);
        ack(channel, tag);
    }

    // el Vol. 1 marca el catch vacío como problema: acá al menos se imprime el error
    private void ack(Channel channel, long tag) {
        try { channel.basicAck(tag, false); } catch (Exception e) { System.err.println("  [Topic] fallo el ACK: " + e); }
    }
}
