package com.estudio.rabbit.streams;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class StreamConsumidores {

    @RabbitListener(queues = StreamConfig.STREAM_EVENTOS, containerFactory = "streamFactory")
    public void auditoria(String evento, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                          @Header(name = "x-stream-offset", defaultValue = "-1") long offset)
            throws IOException {
        System.out.println("  [Auditoría] offset " + offset + ": " + evento);
        channel.basicAck(tag, false); // solo libera el prefetch: NO borra nada
    }

    @RabbitListener(queues = StreamConfig.STREAM_EVENTOS, containerFactory = "streamFactory")
    public void analitica(String evento, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.println("  [Analítica] recibió: " + evento); // recibe LOS MISMOS eventos
        channel.basicAck(tag, false);
    }
}
